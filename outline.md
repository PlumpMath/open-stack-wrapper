openstack sequence

-> tokens [url username pasword]
<- json-tokens

-> tenants [url token-id]
<- json-tenants

-> endpoints [url username password tenant-name]
<- endpoints-adaptated

-> service-call ["/servers" eps-token compute-publicURL]
<- json-servers

-> service-call ["/images" eps-token compute-publicURL]
<- json-images

-> service-call ["/flavors" eps-token compute-publicURL]
<- json-flavors

-> service-call ["v2.0/networks" eps-token network-publicURL]
<- json-networks

-> create-server [token-eps compute-publicURL server-name flavor-href image-href network-id]


-> create-network [token-eps network-publicURL network-name]
<- json-network

-> create-subnet [token-eps network-publicURL cidr allocation-pool-start allocation-pool-end]
<- json-subnet

-> delete-server [compute-publicURL "servers" server-id]
<- nil=OK

-> delete-network [network-publicURL "v2.0/networks" network-id]
<- nil=OK

-> delete-subnet [network-publicURL "v2.0/subnets" subnet-id]
<- nil=OK
