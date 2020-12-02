What is this?
-------------
- device tree for storing device configurations
- netconf listening on port 2022
- root user with password `root` has full access

Sample entry:
 ```xml
<devices xmlns="http://example.org/yang/device-tree">
  <device>
    <name>Example Router 99</name>
    <config>
      <router xmlns="http://example.org/yang/router-config">
        <router-id>10.0.99.1</router-id>
        <bgp>
          <as-number>65099</as-number>
          <neighbors>
            <id>10.0.88.1</id>
            <remote-as>65088</remote-as>
          </neighbors>
        </bgp>
      </router>
    </config>
  </device>
</devices>
```

How to use this?
----------------
First time:
- `make build vol start`

Kill container, reset volume, start new:
- `make rebirth`

Enter CLI:
- `make cli` then `connect --unix`

```shell
$ make cli
> connect --unix
> status
Current NETCONF session:
  ID          : 1
  Path        : /var/run/netopeer2-server.sock
  Transport   : UNIX
  Capabilities:
    urn:ietf:params:netconf:base:1.0
...
```

Edit config:
```shell
$ netconf-console -u root -p root --edit-config startup/devices.xml
<?xml version='1.0' encoding='UTF-8'?>
<nc:ok xmlns:nc="urn:ietf:params:xml:ns:netconf:base:1.0"/>
```

Get config:
```shell
$ netconf-console -u root -p root --get-config
<?xml version='1.0' encoding='UTF-8'?>
<data xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" xmlns:nc="urn:ietf:params:xml:ns:netconf:base:1.0">
...
  <devices xmlns="http://example.org/yang/device-tree">
    <device>
      <name>Example Router 99</name>
      <config>
        <router xmlns="http://example.org/yang/router-config">
          <router-id>10.0.99.1</router-id>
          <bgp>
            <as-number>65099</as-number>
            <neighbors>
              <id>10.0.88.1</id>
              <remote-as>65088</remote-as>
            </neighbors>
          </bgp>
        </router>
      </config>
    </device>
  </devices>
</data>
```
