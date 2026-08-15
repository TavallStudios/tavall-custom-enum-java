# Tavall Java Utils Platform Contract

`tavall-java-utils` is a Tavall-owned Java consumer library and is **not** one of the nine canonical `tavall-java-tools` implementation repositories. It therefore follows the org-wide Tavall DI baseline.

The CustomEnum utility remains intentionally focused and does not gain unrelated runtime responsibilities from this rule. Tavall DI is available as the common Tavall-owned composition/lifecycle contract for consumers and future utility integration points; the library does not become a module scanner, dependency container, or runtime owner.

The remaining Tavall Java Tools stay concern-driven. Do not add Cache, Concurrency, Database, EventBus, Logging, Reflection, Registry, or Scheduler unless the utility repository genuinely owns that concern. If such a concern appears, use the owning Tavall tool rather than building a second generic implementation here.

This keeps low-level utility behavior dependency-light while still obeying the single Tavall composition baseline for Java consumers.

Exact Java 25 Gradle/JUnit verification is required before this platform child is reconciled into the CustomEnum foundation and repository staging.