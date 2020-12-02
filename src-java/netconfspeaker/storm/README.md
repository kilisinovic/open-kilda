What is this?
 ------------- 
 - It's basically an MVP of an MVP
 - Apache STORM - Kafka Topology implementation 
 - Netconf client (simplyfied version of Netconf Speaker)
 - "Get-config" bolt 
  
How to use this?
 ---------------- 
  - fetch this branch
  - make sure the frr is up and it contains actual config
  - run `make compile` to generate the fat jar
  - run `make build` to build and run the container
  - run `make clean` to delete the container 
  
 How to test? 
 ------------ 
 - for now the tests are mocked inside the code
 - you run the test by building the code (container)
 - run `docker logs -f <name of the container>` to see the storm output (get config)
 - this will later be changed! 
 
Explain the code!
------------------
- The code is explained mostly with comments
- But still here is the quick recap:
> The apache storm topology is designed to be as generic as possible, altho (for now) it does not actually
> use Kafka or Storm (framework) for that matter ! What you see is just a skeleton(or a template) that we will work with.
> 
> Inputs (sent from OpenKilda via Kafka):
> - xmlpayload ((get) subtree, (edit) configuration etc.)
> - datastore (target; running, candidate)
> - stored in KildaRequests class
>
> Connection to Sysrepo (prepared to):
> - get the hostname(ip address) and port from system environments - with docker run ...
> - get the password from docker secrets and read it as an array of bytes, which are then Base64 encoded
> - for now mocked with created variables inside the Bolt and reading the password from "test.txt"

Dev status
-----------
- [x] netconf client for sysrepo
- [ ] notifications (if actually needed)
- [x] get config bolt
- [ ] edit config bolt
- [ ] (proper) kafka spout
- [ ] docker secrets
- [ ] docker run with env variables
- [ ] pipeline ready




