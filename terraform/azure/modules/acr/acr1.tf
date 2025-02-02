resource "azurerm_container_registry" "acr" {
  for_each            = var.var_acr
  name                = each.key
  location            = each.value.location
  resource_group_name = each.value.resource_group_name
  sku                 = each.value.sku
  admin_enabled       = each.value.admin_enabled

}

output "op_acr_id" {
  value = {
    for k, v in azurerm_container_registry.acr : k => v.id
  }
}

output "op_acr_login_server" {
  value = { for k, v in azurerm_container_registry.acr : k => v.login_server }

}
