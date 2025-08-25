# Python API Client (Proof of Concept)

This directory contains a limited-functionality Python client for interacting 
with the OAEBUDT Dataspace API. Its primary purpose is to provide a working 
example of how to handle the token authentication logic.

Currently, it can be used to create and fetch participant groups, as well as 
create a file-based report. 

---

## ⚠️ Important Disclaimer

* **Proof of Concept:** This client is a proof of concept and has **not** been 
thoroughly tested. Use it as a reference, not as production-ready code.
* **No Pip Package:** While the directory structure is set up to mimic a 
standard Python package, it is **not** currently distributed or installable 
via `pip`.
* **Limited Scope:** The client currently only implements basic authentication 
and example methods. It can be expanded in the future to interact with more 
API endpoints.

---

## File Structure

* `/oaebudt_dataspace/`: This directory acts as the Python package, 
containing the client's source code.
* `/demo.py`: This file contains example usage patterns for the client.

---

## How to Use

The `demo.py` file is for reference only and should not be run directly. To 
test the client, you should use its code snippets in a Python shell or adapt 
them for your own scripts.

### Example: Using the Client Interactively

1.  Navigate to the root of this directory in your terminal.
2.  Start a Python interpreter (`python`, `python3` or `ipython`).
3.  Import and instantiate the client as shown below.

```python
# Import the client class from the local package
from oaebudt_dataspace import DataspaceClient


# Replace with your actual configuration values
client = DataspaceClient(
    domain_name="your_domain_name",
    realm_name="your_realm_name",
    client_id="your_client_id",
    client_secret="your_client_secret",
    username="your_username",
    password="your_password",
)
```

The client can now be used to interact with the API. It can perform get- and 
post-requests, which will automatically handle authentication in the 
background. 

Additional methods include

 * **client.create_participant_group()**
 * **client.get_participant_group()**
 * **client.create_report()**
 

See the `demo.py` file for examples.
