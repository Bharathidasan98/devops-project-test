variable "var_aks" {
  type = map(object({
    location            = string
    resource_group_name = string
    #dns_prefix          = string
    type = string
    #default_node_pool = object({
    name       = string
    node_count = number
    vm_size    = string
    # })
    vnet_subnet_id = optional(string)
    network_plugin = optional(string)

  }))
}