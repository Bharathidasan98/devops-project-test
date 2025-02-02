variable "var_acr" {
  type = map(object({
    location            = string
    resource_group_name = string
    sku                 = string
    admin_enabled       = bool
  }))
}

