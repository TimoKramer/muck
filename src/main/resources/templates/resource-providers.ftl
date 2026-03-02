<#import "layout.ftl" as layout>

<@layout.page title="Resource Providers - Muck" activePage="resource-providers" connected=connected bobUrl=bobUrl>
    <div id="htmx-container"
         hx-get="/resource-providers"
         hx-trigger="every 10s"
         hx-target="this"
         hx-select="#htmx-content"
         hx-indicator="#loading-indicator">
        <div id="htmx-content">
            <div class="flex items-center justify-between p-4 border-b border-base-300">
                <h2 class="font-semibold text-lg">Resource Providers</h2>
                <button class="btn btn-neutral btn-sm" onclick="createProviderModal.showModal()">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
                    </svg>
                    New Provider
                </button>
            </div>

            <div class="p-4">
                <#if providers?has_content>
                    <div class="overflow-x-auto">
                        <table class="table table-lg">
                            <thead>
                                <tr>
                                    <th>Name</th>
                                    <th>URL</th>
                                    <th class="w-20">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <#list providers as provider>
                                    <tr class="hover">
                                        <td class="font-mono">${provider.name!""}</td>
                                        <td class="text-sm">${provider.url!""}</td>
                                        <td>
                                            <button class="btn btn-ghost btn-xs text-error"
                                                    hx-delete="/resource-providers?name=${provider.name!""}"
                                                    hx-swap="none"
                                                    hx-confirm="Delete resource provider ${provider.name!""}?">
                                                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                                                    <path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                                </svg>
                                            </button>
                                        </td>
                                    </tr>
                                </#list>
                            </tbody>
                        </table>
                    </div>
                <#else>
                    <div class="alert alert-warning">
                        <svg xmlns="http://www.w3.org/2000/svg" class="stroke-current shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                        </svg>
                        <div>
                            <h3 class="font-bold">No resource providers found</h3>
                            <div class="text-sm">Add a resource provider to enable external resources for pipelines.</div>
                        </div>
                    </div>
                </#if>
            </div>
        </div>
    </div>

    <!-- Create Resource Provider Modal -->
    <dialog id="createProviderModal" class="modal">
        <div class="modal-box">
            <h3 class="font-bold text-lg mb-4">Add Resource Provider</h3>
            <form id="createProviderForm" class="space-y-4"
                  hx-post="/resource-providers"
                  hx-target="#createProviderError"
                  hx-swap="innerHTML"
                  hx-indicator="#createProviderLoading">
                <div class="form-control">
                    <label class="label">
                        <span class="label-text">Name</span>
                    </label>
                    <input type="text" name="name" placeholder="e.g., my-provider"
                           class="input input-bordered w-full" required>
                </div>
                <div class="form-control">
                    <label class="label">
                        <span class="label-text">URL</span>
                    </label>
                    <input type="text" name="url" placeholder="e.g., http://provider:8000"
                           class="input input-bordered w-full" required>
                </div>
                <div id="createProviderError" class="alert alert-error hidden">
                </div>
                <div class="modal-action">
                    <button type="button" class="btn" onclick="createProviderModal.close()">Cancel</button>
                    <button type="submit" class="btn btn-neutral">
                        <span class="loading loading-spinner loading-sm htmx-indicator" id="createProviderLoading"></span>
                        Add
                    </button>
                </div>
            </form>
        </div>
        <form method="dialog" class="modal-backdrop">
            <button>close</button>
        </form>
    </dialog>

    <script>
        document.body.addEventListener('htmx:afterRequest', function(evt) {
            if (evt.detail.elt.id === 'createProviderForm') {
                var errorDiv = document.getElementById('createProviderError');
                if (evt.detail.successful) {
                    createProviderModal.close();
                    evt.detail.elt.reset();
                    errorDiv.classList.add('hidden');
                    htmx.ajax('GET', '/resource-providers', {target: '#htmx-container', swap: 'innerHTML', select: '#htmx-content'});
                } else {
                    errorDiv.classList.remove('hidden');
                    errorDiv.textContent = evt.detail.xhr.responseText || 'Failed to create resource provider';
                }
            }

            if (evt.detail.requestConfig && evt.detail.requestConfig.verb === 'delete' && evt.detail.successful) {
                htmx.ajax('GET', '/resource-providers', {target: '#htmx-container', swap: 'innerHTML', select: '#htmx-content'});
            }
        });
    </script>
</@layout.page>
