resource "azurerm_virtual_network" "prod_vnet" {
  name                = "prod-network"
  resource_group_name = module.rg.op_rg_name["prod_rg"]
  location            = module.rg.op_rg_location["prod_rg"]
  address_space       = ["10.254.0.0/16"]
}

resource "azurerm_subnet" "prod_subnet" {
  name                 = "prod-subnet"
  resource_group_name  = module.rg.op_rg_name["prod_rg"]
  virtual_network_name = azurerm_virtual_network.prod_vnet.name
  address_prefixes     = ["10.254.0.0/24"]
  
  delegation {
    name = "aciDelegation"
    service_delegation {
      name    = "Microsoft.ContainerInstance/containerGroups"
      actions = ["Microsoft.Network/virtualNetworks/subnets/action"]
    }
  }
}

resource "azurerm_public_ip" "prod_ip" {
  name                = "prod-pip"
  resource_group_name = module.rg.op_rg_name["prod_rg"]
  location            = module.rg.op_rg_location["prod_rg"]
  allocation_method   = "Static"
}

# since these variables are re-used - a locals block makes this more maintainable
locals {
  backend_address_pool_name      = "${azurerm_virtual_network.prod_vnet.name}-beap"
  frontend_port_name             = "${azurerm_virtual_network.prod_vnet.name}-feport"
  frontend_ip_configuration_name = "${azurerm_virtual_network.prod_vnet.name}-feip"
  http_setting_name              = "${azurerm_virtual_network.prod_vnet.name}-be-htst"
  listener_name                  = "${azurerm_virtual_network.prod_vnet.name}-httplstn"
  request_routing_rule_name      = "${azurerm_virtual_network.prod_vnet.name}-rqrt"
  redirect_configuration_name    = "${azurerm_virtual_network.prod_vnet.name}-rdrcfg"
}

resource "azurerm_application_gateway" "network" {
  name                = "prod-appgateway"
  resource_group_name = module.rg.op_rg_name["prod_rg"]
  location            = module.rg.op_rg_location["prod_rg"]

  sku {
    name     = "Standard_v2"
    tier     = "Standard_v2"
    capacity = 2
  }

  gateway_ip_configuration {
    name      = "my-gateway-ip-configuration"
    subnet_id = azurerm_subnet.prod_subnet.id
  }

  frontend_port {
    name = local.frontend_port_name
    port = 80
  }

  frontend_ip_configuration {
    name                 = local.frontend_ip_configuration_name
    public_ip_address_id = azurerm_public_ip.prod_ip.id
  }

  backend_address_pool {
    name = local.backend_address_pool_name
  }

  backend_http_settings {
    name                  = local.http_setting_name
    cookie_based_affinity = "Disabled"
    path                  = "/path1/"
    port                  = 80
    protocol              = "Http"
    request_timeout       = 60
  }

  http_listener {
    name                           = local.listener_name
    frontend_ip_configuration_name = local.frontend_ip_configuration_name
    frontend_port_name             = local.frontend_port_name
    protocol                       = "Http"
  }

  request_routing_rule {
    name                       = local.request_routing_rule_name
    priority                   = 9
    rule_type                  = "Basic"
    http_listener_name         = local.listener_name
    backend_address_pool_name  = local.backend_address_pool_name
    backend_http_settings_name = local.http_setting_name
  }
}