
# Resource group
module "rg" {
  source = "./modules/rg"
  var_rg = {
    prod_rg = {
      location = "southindia"
    }
    test_rg = {
       location = "southindia"
    }
  }
}
# ACR Module
module "acr" {
  source = "./modules/acr"
  var_acr = {
    proddevecr = {
      resource_group_name = module.rg.op_rg_name["prod_rg"]
      location            = module.rg.op_rg_location["prod_rg"]
      sku                 = "Standard"
      admin_enabled       = true
    }
  }
}
# AKS Module
module "aks" {
  source = "./modules/aks"
  var_aks = {
    prod-dev-cluster = {
      resource_group_name = module.rg.op_rg_name["prod_rg"]
      location            = module.rg.op_rg_location["prod_rg"]
      type                = "SystemAssigned"
      name                = "default"
      node_count          = 1
      vm_size             = "Standard_DS2_v2"
      vnet_subnet_id = azurerm_subnet.prod_subnet.id
      network_plugin   = "azure"
      
    }
    test-dev-cluster = {
      resource_group_name = module.rg.op_rg_name["test_rg"]
      location            = module.rg.op_rg_location["test_rg"]
      type                = "SystemAssigned"
      name                = "default"
      node_count          = 1
      vm_size             = "Standard_DS2_v2"
      network_plugin   = "none"
    } 
  }
}

resource "azurerm_role_assignment" "prod_aks_acr_link" {
  principal_id                     = module.aks.op_aks_object_id["prod-dev-cluster"]
  role_definition_name             = "AcrPull"
  scope                            = module.acr.op_acr_id["proddevecr"]
  skip_service_principal_aad_check = true
}

resource "azurerm_role_assignment" "test_aks_acr_link" {
  principal_id                     = module.aks.op_aks_object_id["test-dev-cluster"]
  role_definition_name             = "AcrPull"
  scope                            = module.acr.op_acr_id["proddevecr"]
  skip_service_principal_aad_check = true
}

#Allow the AKS cluster to manage the subnet
resource "azurerm_role_assignment" "subnet_role" {
  principal_id         = module.aks.op_aks_principal_id["prod-dev-cluster"] #azurerm_kubernetes_cluster.aks.identity[0].principal_id
  role_definition_name = "Network Contributor"
  scope                = azurerm_subnet.prod_subnet.id
}
