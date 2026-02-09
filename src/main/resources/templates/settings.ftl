<#import "layout.ftl" as layout>

<@layout.page title="Settings - Muck" activePage="settings">
    <div id="settings-page" data-signals="{activeTab: 'resource-providers', newName: '', newUrl: ''}">
        <div class="flex items-center justify-between p-4 border-b border-base-300">
            <div class="flex items-center gap-3">
                <a href="/pipelines" class="btn btn-ghost btn-sm btn-circle">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                    </svg>
                </a>
                <h2 class="font-semibold text-lg">Settings</h2>
            </div>
            <#if connected>
                <div class="badge badge-outline badge-sm gap-1 text-success border-success" title="${bobUrl}">
                    <span class="w-1.5 h-1.5 rounded-full bg-success animate-pulse"></span>
                    Connected
                </div>
            <#else>
                <div class="badge badge-outline badge-sm gap-1 text-error border-error">
                    <span class="w-1.5 h-1.5 rounded-full bg-error"></span>
                    Disconnected
                </div>
            </#if>
        </div>

        <div class="p-4">
            <!-- Tabs -->
                <div class="tabs tabs-boxed mb-4">
                    <button class="tab"
                            data-class="{'tab-active': $activeTab === 'resource-providers'}"
                            data-on-click="$activeTab = 'resource-providers'">
                        Resource Providers
                    </button>
                    <button class="tab"
                            data-class="{'tab-active': $activeTab === 'artifact-stores'}"
                            data-on-click="$activeTab = 'artifact-stores'">
                        Artifact Stores
                    </button>
                    <button class="tab"
                            data-class="{'tab-active': $activeTab === 'loggers'}"
                            data-on-click="$activeTab = 'loggers'">
                        Loggers
                    </button>
                    <button class="tab"
                            data-class="{'tab-active': $activeTab === 'cluster'}"
                            data-on-click="$activeTab = 'cluster'">
                        Cluster Info
                    </button>
                </div>

                <!-- Resource Providers Tab -->
                <div data-show="$activeTab === 'resource-providers'">
                    <div class="mb-4">
                        <h3 class="font-medium mb-2">Add Resource Provider</h3>
                        <div class="flex gap-2">
                            <input type="text" placeholder="Name" class="input input-bordered input-sm flex-1"
                                   data-bind="$newName">
                            <input type="text" placeholder="URL" class="input input-bordered input-sm flex-1"
                                   data-bind="$newUrl">
                            <button class="btn btn-primary btn-sm"
                                    data-on-click="@post('/api/resource-providers', {body: JSON.stringify({name: $newName, url: $newUrl}), headers: {'Content-Type': 'application/json'}}).then(() => { $newName = ''; $newUrl = ''; location.reload(); })">
                                Add
                            </button>
                        </div>
                    </div>

                    <div class="overflow-x-auto">
                        <table class="table table-sm">
                            <thead>
                                <tr>
                                    <th>Name</th>
                                    <th>URL</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <#if resourceProviders?has_content>
                                    <#list resourceProviders as provider>
                                        <tr>
                                            <td class="font-mono">${provider.name!""}</td>
                                            <td class="text-sm">${provider.url!""}</td>
                                            <td>
                                                <button class="btn btn-ghost btn-xs text-error"
                                                        data-on-click="if(confirm('Delete resource provider ${provider.name!""}?')) @delete('/api/resource-providers?name=${provider.name!""}').then(() => location.reload())">
                                                    Delete
                                                </button>
                                            </td>
                                        </tr>
                                    </#list>
                                <#else>
                                    <tr>
                                        <td colspan="3" class="text-center text-base-content/60">No resource providers configured</td>
                                    </tr>
                                </#if>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Artifact Stores Tab -->
                <div data-show="$activeTab === 'artifact-stores'">
                    <div class="mb-4">
                        <h3 class="font-medium mb-2">Add Artifact Store</h3>
                        <div class="flex gap-2">
                            <input type="text" placeholder="Name" class="input input-bordered input-sm flex-1"
                                   data-bind="$newName">
                            <input type="text" placeholder="URL" class="input input-bordered input-sm flex-1"
                                   data-bind="$newUrl">
                            <button class="btn btn-primary btn-sm"
                                    data-on-click="@post('/api/artifact-stores', {body: JSON.stringify({name: $newName, url: $newUrl}), headers: {'Content-Type': 'application/json'}}).then(() => { $newName = ''; $newUrl = ''; location.reload(); })">
                                Add
                            </button>
                        </div>
                    </div>

                    <div class="overflow-x-auto">
                        <table class="table table-sm">
                            <thead>
                                <tr>
                                    <th>Name</th>
                                    <th>URL</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <#if artifactStores?has_content>
                                    <#list artifactStores as store>
                                        <tr>
                                            <td class="font-mono">${store.name!""}</td>
                                            <td class="text-sm">${store.url!""}</td>
                                            <td>
                                                <button class="btn btn-ghost btn-xs text-error"
                                                        data-on-click="if(confirm('Delete artifact store ${store.name!""}?')) @delete('/api/artifact-stores?name=${store.name!""}').then(() => location.reload())">
                                                    Delete
                                                </button>
                                            </td>
                                        </tr>
                                    </#list>
                                <#else>
                                    <tr>
                                        <td colspan="3" class="text-center text-base-content/60">No artifact stores configured</td>
                                    </tr>
                                </#if>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Loggers Tab -->
                <div data-show="$activeTab === 'loggers'">
                    <div class="mb-4">
                        <h3 class="font-medium mb-2">Add Logger</h3>
                        <div class="flex gap-2">
                            <input type="text" placeholder="Name" class="input input-bordered input-sm flex-1"
                                   data-bind="$newName">
                            <input type="text" placeholder="URL" class="input input-bordered input-sm flex-1"
                                   data-bind="$newUrl">
                            <button class="btn btn-primary btn-sm"
                                    data-on-click="@post('/api/loggers', {body: JSON.stringify({name: $newName, url: $newUrl}), headers: {'Content-Type': 'application/json'}}).then(() => { $newName = ''; $newUrl = ''; location.reload(); })">
                                Add
                            </button>
                        </div>
                    </div>

                    <div class="overflow-x-auto">
                        <table class="table table-sm">
                            <thead>
                                <tr>
                                    <th>Name</th>
                                    <th>URL</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <#if loggers?has_content>
                                    <#list loggers as logger>
                                        <tr>
                                            <td class="font-mono">${logger.name!""}</td>
                                            <td class="text-sm">${logger.url!""}</td>
                                            <td>
                                                <button class="btn btn-ghost btn-xs text-error"
                                                        data-on-click="if(confirm('Delete logger ${logger.name!""}?')) @delete('/api/loggers?name=${logger.name!""}').then(() => location.reload())">
                                                    Delete
                                                </button>
                                            </td>
                                        </tr>
                                    </#list>
                                <#else>
                                    <tr>
                                        <td colspan="3" class="text-center text-base-content/60">No loggers configured</td>
                                    </tr>
                                </#if>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Cluster Info Tab -->
                <div data-show="$activeTab === 'cluster'">
                    <div class="bg-base-200 rounded-lg p-4">
                        <h3 class="font-medium mb-2">Cluster Information</h3>
                        <#if clusterInfo?has_content>
                            <pre class="text-xs overflow-x-auto"><code>${clusterInfo?keys?join(", ")}</code></pre>
                            <div class="mt-2 space-y-1">
                                <#list clusterInfo as key, value>
                                    <div class="flex">
                                        <span class="font-mono text-sm w-32">${key}:</span>
                                        <span class="text-sm">${value!""}</span>
                                    </div>
                                </#list>
                            </div>
                        <#else>
                            <p class="text-base-content/60">Unable to fetch cluster information</p>
                        </#if>
                    </div>

                    <div class="mt-4">
                        <h3 class="font-medium mb-2">Quick Links</h3>
                        <div class="flex gap-2">
                            <a href="/metrics" target="_blank" class="btn btn-outline btn-sm">
                                Prometheus Metrics
                            </a>
                            <a href="/cctray.xml" target="_blank" class="btn btn-outline btn-sm">
                                CCTray XML
                            </a>
                            <a href="/api/cluster/info" target="_blank" class="btn btn-outline btn-sm">
                                Cluster Info JSON
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
</@layout.page>
