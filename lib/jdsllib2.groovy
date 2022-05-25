void setBuildNumber(String job_new = "", String job_old = "") {
    def job_old_obj = Jenkins.instance.getItemByFullName(job_old)
    def job_new_obj = Jenkins.instance.getItemByFullName(job_new)
    job_new_obj.updateNextBuildNumber(job_old_obj.getNextBuildNumber())
}


void setDirs(List dirs) {
    for(dir_item in dirs) {
        folder(dir_item)
    }
}

// return ["gvar_list": gvar_default, "token_str": token_str, "regex_text": regex_text, "regex_expr": regex_expr]
Map getTriggerArgs(def token_str = "", def regex_text = "", def regex_expr = "", def gvar_list = null) {
    def gvar_default = [
        "BRANCH":          ["\$.ref",             "JSONPath"],
        "REPOSITORY":      ["\$.repository.name", "JSONPath"],
        "GIT_AUTHOR":      ["\$.pusher.name",     "JSONPath"],
        "GIT_HEAD_COMMIT": ["\$.head_commit.url", "JSONPath"]
    ]

    if(gvar_list) {
        for(gvar_item in gvar_list) {
            gvar_default[gvar_item.key] = gvar_item.value
        }
    }

    def triggerArgs = [
        "gvar_list":  gvar_default,
        "token_str":  token_str,
        "regex_text": regex_text,
        "regex_expr": regex_expr
    ]

    return triggerArgs
}

// return ["str_params": str_params_default, "ac_params": ac_params_default]
Map getParamArgs(def str_params = null, def ch_params = null, def ac_params = null) {
    def str_params_default = [:]

    if(str_params) {
        for(param_item in str_params) {
            str_params_default[param_item.key] = param_item.value
        }
    }

    def ch_params_default = [:]

    if(ch_params) {
        for(param_item in ch_params) {
            ch_params_default[param_item.key] = param_item.value
        }
    }

    def ac_params_default = [:]

    if(ac_params) {
        for(param_item in ac_params) {
            ac_params_default[param_item.key] = param_item.value
        }
    }

    def paramArgs = [
        "str_params": str_params_default,
        "ch_params":  ch_params_default,
        "ac_params":  ac_params_default
    ]

    return paramArgs
}


def setPipelineJob(String work_dir = "", String job_name, String pp_script = "", def trigger_args = null, def param_args = null) {
    def job_item = pipelineJob(job_name) {
      logRotator {
          artifactNumToKeep(100)
          numToKeep(200)
      }
    }

    // job_item.disabled()

    if(!work_dir) {
        work_dir = "."
    }

    job_item.definition {
        cps {
            if(pp_script) {
                script(readFileFromWorkspace(work_dir + "/" + job_name + "/" + pp_script))
            } else {
                script(pp_script)
            }
            sandbox()
        }
    } // End of base definition

    if(trigger_args) {
        job_item.properties {
            pipelineTriggers {
                triggers {
                    genericTrigger {
                        genericVariables {
                            for(gvar_item in trigger_args.gvar_list) {
                                // Sandboxed System Groovy Scripts don't support multiple assignments
                                def var_val = gvar_item.value[0]
                                def var_exp = gvar_item.value[1]

                                genericVariable {
                                    key(gvar_item.key)
                                    value(var_val)
                                    if(!var_exp) {
                                        // Optional, defaults to JSONPath
                                        expressionType("JSONPath")
                                    }
                                    // Optional, defaults to empty string
                                    regexpFilter("")
                                    // Optional, defaults to empty string
                                    defaultValue("")
                                } // End of genericVariable
                            } // End of for
                        }

                        token(trigger_args.token_str)
                        tokenCredentialId("")
                        printContributedVariables(true)
                        printPostContent(true)
                        silentResponse(false)
                        regexpFilterText(trigger_args.regex_text)
                        regexpFilterExpression(trigger_args.regex_expr)
                    } // End of genericTrigger
                } // End of triggers
            } // End of pipelineTriggers
        }
    } // End of trigger definition

    if(param_args) {
        // param_list = [[param_name, param_default, param_descr], ...]
        if(param_args.str_params) {
            job_item.parameters {
                for(str_item in param_args.str_params) {
                    def param_default = str_item.value[0]
                    def param_descr   = str_item.value[1]

                    stringParam(str_item.key, param_default, param_descr)
                }
            }
        } // End of string params definition

        if(param_args.ch_params) {
            // param_list = [param_name: [param1, param2, ...], param_descr], ...]
            job_item.parameters {
                for(ch_item in param_args.ch_params) {
                    def param_list  = ch_item.value[0]
                    def param_descr = ch_item.value[1]

                    choiceParam(ch_item.key, param_list, param_descr)
                }
            }
        } // End of choice params definition

        // param_list = [[param_name, param_type, param_script, param_fallback], ...]
        if(param_args.ac_params) {
            for(ac_item in param_args.ac_params) {
                def param_type     = ac_item.value[0]
                def param_script   = ac_item.value[1]
                def param_fallback = ac_item.value[2]

                switch(param_type) {
                    case "SINGLE_SELECT":
                    case  "MULTI_SELECT":
                    case      "CHECKBOX":
                    case         "RADIO":
                        job_item.parameters {
                            activeChoiceReactiveParam(ac_item.key) {
                                choiceType(param_type)
                                groovyScript {
                                    if(param_script) {
                                        script(readFileFromWorkspace(work_dir + "/" + job_name + "/" + param_script))
                                    } else {
                                        script(param_script)
                                    }

                                    if(param_fallback) {
                                        fallbackScript(readFileFromWorkspace(work_dir + "/" + job_name + "/" + param_fallback))
                                    } else {
                                        fallbackScript(param_fallback)
                                    }
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
                            activeChoiceReactiveReferenceParam(ac_item.key) {
                                choiceType(param_type)
                                groovyScript {
                                    if(param_script) {
                                        script(readFileFromWorkspace(work_dir + "/" + job_name + "/" + param_script))
                                    } else {
                                        script(param_script)
                                    }

                                    if(param_fallback) {
                                        fallbackScript(readFileFromWorkspace(work_dir + "/" + job_name + "/" + param_fallback))
                                    } else {
                                        fallbackScript(param_fallback)
                                    }
                                }
                            }
                        }
                        break
                } // End of switch
            } // End of for
        } // End of AC definition
    } // End of params definition
    return job_item
}

// Return job_item
def wrapTrigger(def job_item, String schedule = "", def parameterized = false) {
    if(schedule) {
        job_item.properties {
            pipelineTriggers {
                triggers {
                    if(parameterized) {
                        parameterizedCron { parameterizedSpecification(schedule) }
                    } else {
                        cron { spec(schedule) }
                    }
                }
            }
        }
    }

    return job_item
}
