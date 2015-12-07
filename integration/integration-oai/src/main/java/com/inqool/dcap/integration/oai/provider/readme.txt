Description of how this OAI-PMH provider works:

OaiPmhResource class listens to HTTP requests whose parameters are mapped to Request classes,
and depending on the verb parameter it delegates requests on one of resource classes implementing VerbResource.
This *Resource then constructs the response with ways provided by OAIConfiguration class.

OAIConfiguration is the key point of this provider and all users should start looking there.
The class provides resource classes with ways to load data in various formats.
