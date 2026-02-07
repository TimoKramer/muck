<#if pipelines?has_content>
    <div class="space-y-3">
        <#list pipelines as pipeline>
            <#assign idx = pipeline?index>
            <a class="block pipeline-row"
               href="/runs?group=${pipeline.group}&name=${pipeline.name}"
               data-group="${pipeline.group}"
               data-class="{'ring-2 ring-primary': $focusedIndex === ${idx}}"
               data-show="(!$filter || '${pipeline.name?lower_case}'.includes($filter.toLowerCase()) || '${pipeline.group?lower_case}'.includes($filter.toLowerCase())) && (!$selectedGroup || $selectedGroup === '${pipeline.group}')">
                <div class="rounded-lg border border-base-300 bg-base-100 hover:border-base-content/20 transition-colors duration-150 cursor-pointer p-4">
                    <div class="flex items-center justify-between">
                        <div class="flex-1">
                            <h3 class="font-medium">${pipeline.name}</h3>
                            <div class="text-sm text-base-content/60 mt-1">${pipeline.group}</div>
                        </div>
                        <div class="flex items-center gap-2">
                            <a href="/logs?group=${pipeline.group}&name=${pipeline.name}&run=${pipeline.runId}"
                               data-on-click="$event.stopPropagation()">
                                <#if pipeline.status == "running">
                                    <span class="badge badge-outline badge-sm gap-1 text-info border-info hover:bg-info/10">
                                        <span class="loading loading-spinner loading-xs"></span>
                                        ${pipeline.status}
                                    </span>
                                <#elseif pipeline.status == "passed">
                                    <span class="badge badge-outline badge-sm text-success border-success hover:bg-success/10">${pipeline.status}</span>
                                <#elseif pipeline.status == "failed">
                                    <span class="badge badge-outline badge-sm text-error border-error hover:bg-error/10">${pipeline.status}</span>
                                <#else>
                                    <span class="badge badge-outline badge-sm hover:bg-base-content/10">${pipeline.status}</span>
                                </#if>
                            </a>
                            <button class="btn btn-ghost btn-xs btn-circle"
                                    title="Start pipeline"
                                    data-on-click="$event.stopPropagation(); $event.preventDefault(); @post('/start?group=${pipeline.group}&name=${pipeline.name}')">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <circle cx="12" cy="12" r="10"/>
                                    <path d="M10 8l6 4-6 4V8z" fill="currentColor" stroke="none"/>
                                </svg>
                            </button>
                        </div>
                    </div>
                </div>
            </a>
        </#list>
    </div>
<#else>
    <div class="alert alert-warning">
        <svg xmlns="http://www.w3.org/2000/svg" class="stroke-current shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
        <div>
            <h3 class="font-bold">No pipelines found</h3>
            <div class="text-sm">Make sure Bob is running at the configured URL.</div>
        </div>
    </div>
</#if>
