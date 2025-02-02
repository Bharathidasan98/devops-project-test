resource "azurerm_kubernetes_cluster" "aks" {
  for_each            = var.var_aks
  name                = each.key
  location            = each.value.location
  resource_group_name = each.value.resource_group_name
  dns_prefix          = "${each.key}-dns"

  default_node_pool {
    name       = each.value.name
    node_count = each.value.node_count
    vm_size    = each.value.vm_size
    vnet_subnet_id = each.value.vnet_subnet_id
  }

  identity {
    type = each.value.type
  }

  network_profile {
    network_plugin = each.value.network_plugin
  }
  tags = {
    Environment = "Production"
  }

}

output "op_aks_fqdn" {
  description = "The FQDN of the AKS cluster"
  #value       = azurerm_kubernetes_cluster.aks.fqdn
  value = { for k, v in azurerm_kubernetes_cluster.aks : k => v.fqdn }
}

output "op_aks_object_id" {
  #  value = azurerm_kubernetes_cluster.aks.kubelet_identity[0].object_id
  value = { for k, v in azurerm_kubernetes_cluster.aks : k => v.kubelet_identity[0].object_id
  }
}

output "op_aks_principal_id" {
  value = {
     for k, v in azurerm_kubernetes_cluster.aks : k => v.identity[0].principal_id
  }
}

