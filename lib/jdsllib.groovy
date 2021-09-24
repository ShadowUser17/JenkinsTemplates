package org.jdsllib

void setDirs(List dirs) {
    for(dir_item in dirs) {
        folder(dir_item)
    }
}


void setActiveChoiceParam(def job_item, String param_name, String param_type, String param_script = "", String param_fallback = "") {
    switch(param_type) {
        case "SINGLE_SELECT":
        case  "MULTI_SELECT":
        case      "CHECKBOX":
        case         "RADIO":
            job_item.parameters {
                activeChoiceReactiveParam(param_name) {
                    choiceType(param_type)
                    groovyScript {
                        script(param_script)
                        fallbackScript(param_fallback)
                    }
                }
            }
            return

        case              "TEXT_BOX":
        case        "FORMATTED_HTML":
        case "FORMATTED_HIDDEN_HTML":
        case          "ORDERED_LIST":
        case        "UNORDERED_LIST":
            job_item.parameters {
                activeChoiceReactiveReferenceParam(param_name) {
                    choiceType(param_type)
                    groovyScript {
                        script(param_script)
                        fallbackScript(param_fallback)
                    }
                }
            }
            return
    }
}

// gvars_list = [[var_key, var_val, var_exp], ...]
void setGenericTrigger(def job_item, List gvars_list, String token_str, String token_id, String regex_text, String regex_expression) {
    job_item.properties {
        pipelineTriggers {
            triggers {
                genericTrigger {
                    genericVariables {
                        for(gvars_item in gvars_list) {
                            def(var_key, var_val, var_exp) = gvars_item

                            genericVariable {
                                key(var_key)
                                value(var_val)
                                if(var_exp == "") {
                                    // Optional, defaults to JSONPath
                                    expressionType("JSONPath")
                                }
                                // Optional, defaults to empty string
                                regexpFilter("")
                                // Optional, defaults to empty string
                                defaultValue("")
                            } // end of genericVariable
                        }        
                    }

                    /*genericRequestVariables {
                        genericRequestVariable {
                            key("requestParameterName")
                            regexpFilter("")
                        }
                    }*/

                    /*genericHeaderVariables {
                        genericHeaderVariable {
                            key("requestHeaderName")
                            regexpFilter("")
                        }
                    }*/

                    token(token_str)
                    tokenCredentialId(token_id)
                    printContributedVariables(true)
                    printPostContent(true)
                    silentResponse(false)
                    regexpFilterText(regex_text)
                    regexpFilterExpression(regex_expression)
                }
            }
        }
    }
}


void setPipelineScript(def job_item, String pp_script = "") {
    job_item.definition {
        cps {
            script(pp_script)
            sandbox()
        }
    }
}


return this
