/*
Dependency list:
- Job DSL
- Authorize Project
- Active Choices
- Generic Webhook Trigger
- Parameterized Scheduler
*/

// Example: [["dir_name", view_list: [[view_name, view_regex], ...]], ...]
void setDirs(List dirs) {
    for(dir_item in dirs) {
        if(!dir_item[1]) {
            folder(dir_item[0])

        } else {
            def view_list = dir_item[1]

            folder(dir_item[0]) {
                views {
                    for(view_item in view_list) {
                        listView(view_item[0]) {
                            recurse(true)

                            columns {
                                status()
                                name()
                                lastSuccess()
                                lastFailure()
                                lastDuration()
                            }

                            configure { view ->
                                view / columns << 'hudson.plugins.cobertura.CoverageColumn' {
                                    type('line')
                                }
                            }

                            jobs { regex(view_item[1]) }
                        }
                    }
                }
            }
        }
    }
}

/*
webhook_args = [token_str, regex_text, regex_expr, gvar_list = [[key, val], ...]]
cron_args = [schedule, parameterized]
*/
def setPipelineTriggers(def job_item, def webhook_args = null, def cron_args = null) {
    job_item.properties {
        pipelineTriggers {
            triggers {
                if(webhook_args) {
                    def gvar_default = [
                        "DELETED":         ["\$.deleted",         "JSONPath"],
                        "BRANCH":          ["\$.ref",             "JSONPath"],
                        "REPOSITORY":      ["\$.repository.name", "JSONPath"],
                        "GIT_AUTHOR":      ["\$.pusher.name",     "JSONPath"],
                        "GIT_HEAD_COMMIT": ["\$.head_commit.url", "JSONPath"]
                    ]

                    def token_str  = webhook_args[0]
                    def regex_text = webhook_args[1]
                    def regex_expr = webhook_args[2]
                    def gvar_list  = webhook_args[3]

                    if(gvar_list) {
                        for(gvar_item in gvar_list) {
                            gvar_default[gvar_item.key] = gvar_item.value
                        }
                    }

                    genericTrigger {
                        genericVariables {
                            for(gvar_item in gvar_default) {
                                // Sandboxed System Groovy Scripts don't support multiple assignments
                                def var_val = gvar_item.value[0]
                                def var_exp = gvar_item.value[1]

                                genericVariable {
                                    key(gvar_item.key)
                                    value(var_val)

                                    // Optional, defaults to JSONPath
                                    if(!var_exp) {
                                        expressionType("JSONPath")
                                    }

                                    // Optional, defaults to empty string
                                    regexpFilter("")
                                    // Optional, defaults to empty string
                                    defaultValue("")
                                } // End of genericVariable
                            } // End of for
                        } // End of genericVariables

                        token(token_str)
                        tokenCredentialId("")
                        printContributedVariables(true)
                        printPostContent(true)
                        silentResponse(false)
                        regexpFilterText(regex_text)
                        regexpFilterExpression(regex_expr)

                    } // End of genericTrigger
                } // End of trigger definition

                if(cron_args) {
                    def schedule      = cron_args[0]
                    def parameterized = cron_args[1]

                    if(parameterized) {
                        parameterizedCron { parameterizedSpecification(schedule) }
                    } else {
                        cron { spec(schedule) }
                    }
                } // End of cron definition

            } // End of triggers
        } // End of pipelineTriggers
    } // End of properties

    return job_item
}

// return ["str_params": str_params_default, "ch_params": ch_params_default, "ac_params": ac_params_default]
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


def setPipelineJob(String work_dir = ".", String job_name, String pp_script = "", def param_args = null, def concurrent = null) {
    def job_item = pipelineJob(job_name) {
      logRotator {
          artifactNumToKeep(30)
          numToKeep(30)
      }
    }

    if(concurrent) {
        job_item.throttleConcurrentBuilds {
            maxTotal(concurrent)
        }
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
