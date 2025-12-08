<#import "layout.ftl" as layout>

<@layout.page title="Logs - Muck" bobUrl=bobUrl>
    <div class="card bg-base-100 shadow-2xl">
        <div class="card-body">
            <div class="mb-4">
                <a href="/" class="btn btn-ghost btn-sm gap-2">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                    </svg>
                    Back to Pipelines
                </a>

                <div class="mt-4 mb-2">
                    <h2 class="text-xl font-bold font-mono">${run}</h2>
                    <div class="text-sm opacity-60 mt-1">Run Logs</div>
                </div>
            </div>

            <div class="form-control w-full">
                <textarea
                    id="logs-output"
                    class="textarea textarea-bordered w-full font-mono text-sm bg-base-200"
                    style="min-height: 500px; resize: vertical;"
                    readonly
                    placeholder="Loading logs..."></textarea>
            </div>

            <div id="logs-status" class="text-sm opacity-60 mt-2">
                <span class="loading loading-spinner loading-xs"></span>
                Connecting...
            </div>
        </div>
    </div>

    <script>
        (function() {
            const textarea = document.getElementById('logs-output');
            const status = document.getElementById('logs-status');
            const runId = '${run?js_string}';

            fetch('/logs/stream?run=' + encodeURIComponent(runId))
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Failed to fetch logs: ' + response.status);
                    }

                    status.innerHTML = '<span class="text-success">Streaming...</span>';

                    const reader = response.body.getReader();
                    const decoder = new TextDecoder();

                    function read() {
                        reader.read().then(({done, value}) => {
                            if (done) {
                                status.innerHTML = '<span class="text-info">Stream completed</span>';
                                return;
                            }

                            const text = decoder.decode(value, {stream: true});
                            textarea.value += text;
                            textarea.scrollTop = textarea.scrollHeight;

                            read();
                        }).catch(err => {
                            status.innerHTML = '<span class="text-error">Error: ' + err.message + '</span>';
                        });
                    }

                    read();
                })
                .catch(err => {
                    status.innerHTML = '<span class="text-error">Error: ' + err.message + '</span>';
                    textarea.placeholder = 'Failed to load logs';
                });
        })();
    </script>
</@layout.page>
