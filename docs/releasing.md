# Release and container publishing

## Current delivery status

The CI workflow verifies the Maven project and builds the container locally. The separate publish-container.yml workflow publishes an image to GitHub Container Registry (GHCR) when a version tag matching v*.*.* is pushed.

A workflow file being present does not mean an image has been published. Check the Actions run and the package page before using the image in a deployment.

## Publish a version

After reviewing the code and confirming CI is green:

1. Open the repository Code page.
2. Open Releases → Draft a new release.
3. Choose Create a new tag, using a semantic version such as v0.1.0.
4. Target the reviewed main commit.
5. Add release notes describing tested behavior and known limitations.
6. Publish the release. The tag push triggers the container publishing workflow.
7. Wait for the Publish Ops Guardian Container workflow to succeed.
8. Open the repository Packages page and verify that ghcr.io/sajuu730/kafka-connect-ops-guardian:v0.1.0 exists.

The first publish may create a private package. If the image is intended for public portfolio use, open the package settings and explicitly change package visibility to Public. Do not change repository visibility or publish credentials.

## Pull and run

After publication and public visibility are confirmed:

    docker pull ghcr.io/sajuu730/kafka-connect-ops-guardian:v0.1.0

Use the immutable version tag for deployment. Avoid relying on latest for reproducible environments.

## Release checklist

- [ ] Latest CI run is green.
- [ ] Release notes accurately describe tested behavior.
- [ ] Version tag points to the reviewed commit.
- [ ] Container publishing workflow succeeds.
- [ ] Package exists in GHCR.
- [ ] Package visibility is intentionally set.
- [ ] Kubernetes manifest image reference is updated to the published version.
- [ ] Live deployment smoke test is completed.

## Security and operational notes

The image contains the application only; it does not include Kafka Connect. The Guardian service must be configured to reach the target Connect REST endpoint. Keep recovery disabled until the cluster, restart budget, incident delivery and rollback procedures have been validated. The current restart budget is process-local; use one Guardian replica while recovery is enabled.
