# DynamicDNS for Forge  
A dynamic DNS plugin for Spigot
### Commands:
* /UpdateIP \[IP\] - Force updates the IP on your DDNS Services, the IP argument can also be used to overide the IP detection on the DDNS services (best to leave it blank)
### Config:
```yaml
# The Configuration File for Steve's DynamicDNS Mod
general {
    S:domain=exampledomain # Your subdomain on DuckDNS.org
    S:period=3600 # The period between updating IPs in seconds (3600 = 1 hour)
    S:token=a7c4d0ad-114e-40ef-ba1d-d217904a50f2 # Your token on DuckDNS.org
}
```