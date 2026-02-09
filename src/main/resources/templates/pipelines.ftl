<#import "layout.ftl" as layout>

<@layout.page title="Pipelines - Muck" activePage="pipelines" connected=connected bobUrl=bobUrl>
    <div id="htmx-container"
         hx-get="/pipelines"
         hx-trigger="every 10s"
         hx-target="this"
         hx-select="#htmx-content"
         hx-indicator="#loading-indicator">
        <div id="htmx-content">
            <div class="flex items-center justify-between p-4 border-b border-base-300">
                <h2 class="font-semibold text-lg">Pipelines</h2>
                <div class="dropdown dropdown-end">
                    <div tabindex="0" role="button" class="btn btn-primary btn-sm">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
                        </svg>
                        New Pipeline
                    </div>
                    <ul tabindex="0" class="dropdown-content menu bg-base-100 rounded-box z-10 w-48 p-2 shadow-lg border border-base-300">
                        <li><a onclick="createPipelineModal.showModal()">Manual Form</a></li>
                        <li><a onclick="uploadPipelineModal.showModal()">Upload File</a></li>
                    </ul>
                </div>
            </div>

            <div class="p-4">
                <#if pipelines?has_content>
                    <div class="overflow-x-auto">
                        <table class="table table-lg">
                            <thead>
                                <tr>
                                    <th>Name</th>
                                    <th>Group</th>
                                    <th>Status</th>
                                    <th class="w-20">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <#list pipelines as pipeline>
                                    <tr class="hover cursor-pointer"
                                        hx-get="/runs?group=${pipeline.group}&name=${pipeline.name}"
                                        hx-target="#htmx-container"
                                        hx-select="#htmx-container"
                                        hx-swap="outerHTML"
                                        hx-indicator="#loading-indicator"
                                        hx-push-url="true">
                                        <td class="font-medium">${pipeline.name}</td>
                                        <td class="text-sm text-base-content/60">${pipeline.group}</td>
                                        <td>
                                            <#if pipeline.status == "running">
                                                <span class="badge badge-outline badge-sm gap-1 text-info border-info">
                                                    <span class="loading loading-spinner loading-xs"></span>
                                                    ${pipeline.status}
                                                </span>
                                            <#elseif pipeline.status == "passed">
                                                <span class="badge badge-outline badge-sm text-success border-success">${pipeline.status}</span>
                                            <#elseif pipeline.status == "failed">
                                                <span class="badge badge-outline badge-sm text-error border-error">${pipeline.status}</span>
                                            <#else>
                                                <span class="badge badge-outline badge-sm">${pipeline.status}</span>
                                            </#if>
                                        </td>
                                        <td>
                                            <button class="btn btn-ghost btn-xs btn-circle"
                                                    hx-post="/start?group=${pipeline.group}&name=${pipeline.name}"
                                                    hx-swap="none"
                                                    onclick="event.stopPropagation()">
                                                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                                    <circle cx="12" cy="12" r="10"/>
                                                    <path d="M10 8l6 4-6 4V8z" fill="currentColor" stroke="none"/>
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
                            <h3 class="font-bold">No pipelines found</h3>
                            <div class="text-sm">Make sure Bob is running at the configured URL.</div>
                        </div>
                    </div>
                </#if>
            </div>
        </div>
    </div>

    <!-- Create Pipeline Modal (Manual Form) -->
    <dialog id="createPipelineModal" class="modal">
        <div class="modal-box max-w-2xl">
            <h3 class="font-bold text-lg mb-4">Create Pipeline</h3>
            <form id="createPipelineForm" class="space-y-4"
                  hx-post="/create"
                  hx-target="#createPipelineError"
                  hx-swap="innerHTML"
                  hx-indicator="#createPipelineLoading">
                <div class="grid grid-cols-2 gap-4">
                    <div class="form-control">
                        <label class="label">
                            <span class="label-text">Group</span>
                        </label>
                        <input type="text" name="group" placeholder="e.g., dev"
                               class="input input-bordered w-full" required>
                    </div>
                    <div class="form-control">
                        <label class="label">
                            <span class="label-text">Name</span>
                        </label>
                        <input type="text" name="name" placeholder="e.g., my-pipeline"
                               class="input input-bordered w-full" required>
                    </div>
                </div>
                <div class="form-control">
                    <label class="label">
                        <span class="label-text">Image</span>
                    </label>
                    <input type="text" name="image" placeholder="e.g., alpine:latest"
                           class="input input-bordered w-full" required>
                </div>
                <div class="form-control">
                    <label class="label">
                        <span class="label-text">Steps (one command per line)</span>
                    </label>
                    <textarea name="steps"
                              placeholder="echo Hello World&#10;ls -la&#10;echo Done"
                              class="textarea textarea-bordered w-full h-32" required></textarea>
                </div>
                <div class="form-control">
                    <label class="label">
                        <span class="label-text">Environment Variables (optional, KEY=VALUE per line)</span>
                    </label>
                    <textarea name="vars"
                              placeholder="DEBUG=true&#10;LOG_LEVEL=info"
                              class="textarea textarea-bordered w-full h-20"></textarea>
                </div>
                <div id="createPipelineError" class="alert alert-error hidden">
                </div>
                <div class="modal-action">
                    <button type="button" class="btn" onclick="createPipelineModal.close()">Cancel</button>
                    <button type="submit" class="btn btn-primary">
                        <span class="loading loading-spinner loading-sm htmx-indicator" id="createPipelineLoading"></span>
                        Create
                    </button>
                </div>
            </form>
        </div>
        <form method="dialog" class="modal-backdrop">
            <button>close</button>
        </form>
    </dialog>

    <!-- Upload Pipeline Modal -->
    <dialog id="uploadPipelineModal" class="modal">
        <div class="modal-box">
            <h3 class="font-bold text-lg mb-4">Upload Pipeline Definition</h3>
            <form id="uploadPipelineForm" class="space-y-4">
                <div class="form-control">
                    <label class="label">
                        <span class="label-text">Pipeline definition file (YAML or JSON)</span>
                    </label>
                    <input type="file" id="pipelineFile" accept=".yaml,.yml,.json"
                           class="file-input file-input-bordered w-full" required>
                </div>
                <div id="uploadPipelineError" class="alert alert-error hidden"></div>
                <div class="modal-action">
                    <button type="button" class="btn" onclick="uploadPipelineModal.close()">Cancel</button>
                    <button type="submit" class="btn btn-primary">Upload</button>
                </div>
            </form>
        </div>
        <form method="dialog" class="modal-backdrop">
            <button>close</button>
        </form>
    </dialog>

    <script>
        document.body.addEventListener('htmx:afterRequest', function(evt) {
            if (evt.detail.elt.id === 'createPipelineForm') {
                var errorDiv = document.getElementById('createPipelineError');
                if (evt.detail.successful) {
                    createPipelineModal.close();
                    evt.detail.elt.reset();
                    errorDiv.classList.add('hidden');
                    htmx.ajax('GET', '/pipelines', {target: '#htmx-content', swap: 'innerHTML', select: '#htmx-content'});
                } else {
                    errorDiv.classList.remove('hidden');
                    errorDiv.textContent = evt.detail.xhr.responseText || 'Failed to create pipeline';
                }
            }
        });

        document.getElementById('uploadPipelineForm').addEventListener('submit', function(evt) {
            evt.preventDefault();
            var file = document.getElementById('pipelineFile').files[0];
            var errorDiv = document.getElementById('uploadPipelineError');
            if (!file) return;

            file.text().then(function(content) {
                var type = file.name.endsWith('.json') ? 'application/json' : 'application/x-yaml';
                return fetch('/create', {method: 'POST', headers: {'Content-Type': type}, body: content});
            }).then(function(res) {
                if (res.ok) {
                    uploadPipelineModal.close();
                    evt.target.reset();
                    errorDiv.classList.add('hidden');
                    htmx.ajax('GET', '/pipelines', {target: '#htmx-content', swap: 'innerHTML', select: '#htmx-content'});
                } else {
                    return res.text().then(function(t) {
                        errorDiv.classList.remove('hidden');
                        errorDiv.textContent = t || 'Failed to create pipeline';
                    });
                }
            }).catch(function(err) {
                errorDiv.classList.remove('hidden');
                errorDiv.textContent = err.message;
            });
        });
    </script>
</@layout.page>
