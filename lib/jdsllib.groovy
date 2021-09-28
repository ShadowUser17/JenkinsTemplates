void setDirs(List dirs) {
    for(dir_item in dirs) {
        folder(dir_item)
    }
}

// sparam_list = [[param_name, param_default, param_descr], ...]
void setStringParams(def job_item, List sparam_list) {
    job_item.parameters {
        for(sparam_item in sparam_list) {
            def(param_name, param_default, param_descr) = sparam_item
            stringParam(param_name, param_default, param_descr)
        }
    }
}

// param_list = [[param_name, param_type, param_script, param_fallback], ...]
void setActiveChoiceParams(def job_item, List param_list) {
    for(param_item in param_list) {
        def(param_name, param_type, param_script, param_fallback) = param_item

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
                break

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
                break
        }
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


def setPipelineJob(String job_name, String pp_script = "") {
    def job_item = pipelineJob(job_name)

    job_item.definition {
        cps {
            script(pp_script)
            sandbox()
        }
    }

    return job_item
}
