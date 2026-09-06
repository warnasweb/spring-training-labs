#!/usr/bin/env python3
"""End-to-end assertions against running Compose apps; no third-party Python dependencies."""
import json, time, uuid, urllib.request, urllib.error
BASE="http://localhost:8080"
PRODUCT="11111111-1111-1111-1111-111111111111"
def request(path,body=None,key=None,method=None):
    headers={"Content-Type":"application/json","X-Correlation-ID":"day7-smoke"}
    if key: headers["Idempotency-Key"]=key
    req=urllib.request.Request(BASE+path,data=json.dumps(body).encode() if body is not None else None,headers=headers,method=method)
    with urllib.request.urlopen(req,timeout=10) as res:
        raw=res.read()
        return json.loads(raw) if raw else None

def wait_for(path,predicate,timeout=120):
    end=time.time()+timeout
    while time.time()<end:
        try:
            result=request(path)
            if predicate(result): return result
        except (OSError,ValueError): pass
        time.sleep(1)
    raise AssertionError("Timed out waiting for "+path)
# Gate on every application, not just the gateway: container startup is concurrent.
for port in range(8080,8086):
    deadline=time.time()+180
    while True:
        try:
            with urllib.request.urlopen(f"http://localhost:{port}/actuator/health/readiness",timeout=5) as response:
                if json.load(response)["status"]=="UP": break
        except (OSError,ValueError): pass
        if time.time()>deadline: raise AssertionError(f"Service {port} is not ready")
        time.sleep(1)
wait_for("/products",lambda value:len(value)>0)
initial=request("/inventory/"+PRODUCT)["available"]
for fail,qty,state in [(False,2,"CONFIRMED"),(True,3,"CANCELLED")]:
    key=str(uuid.uuid4());body={"productId":PRODUCT,"quantity":qty,"failPayment":fail}
    request("/orders",body,key);request("/orders",body,key)
    wait_for("/orders/"+key,lambda o:o["status"]==state)
    wait_for("/notifications/"+key,lambda ns:any(n["event_type"]==("OrderCancelled" if fail else "OrderConfirmed") for n in ns))
    assert len(request("/payments/"+key))==1
    try:
        request("/orders",dict(body,quantity=qty+1),key)
        raise AssertionError("Conflicting idempotency key accepted")
    except urllib.error.HTTPError as e: assert e.code==409
assert request("/inventory/"+PRODUCT)["available"]==initial-2
key=str(uuid.uuid4())
request("/orders",{"productId":PRODUCT,"quantity":initial+1,"failPayment":False},key)
wait_for("/orders/"+key,lambda o:o["status"]=="REJECTED")
assert request("/payments/"+key)==[]
p=request("/products",{"name":"Smoke book","price":9.99})
request("/products/"+p["id"],{"name":"Updated book","price":12.00},method="PUT")
assert request("/products/"+p["id"])["name"]=="Updated book"
request("/products/"+p["id"],method="DELETE")
print("PASS: CRUD, success saga, compensation, idempotency, stock rejection, notifications")
