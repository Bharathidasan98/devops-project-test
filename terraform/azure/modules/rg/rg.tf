resource "azurerm_resource_group" "rg" {
  for_each = var.var_rg
  name     = each.key
  location = each.value.location
}
output "op_rg_name" {
  description = "Resource group name"
  value = { for k, v in azurerm_resource_group.rg : k => v.name
  }
}

output "op_rg_location" {
  description = "Resource group location"
  value = { for k, v in azurerm_resource_group.rg : k => v.location
  }
}