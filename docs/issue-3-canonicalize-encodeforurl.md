# Issue #3: `encodeForURL` Canonicalize Flag Failure

**Tracking ID:** BX-ESAPI / BL-1119
**Repository:** [1234567DN/bx-esapi-issue-3](https://github.com/1234567DN/bx-esapi-issue-3)
**Upstream module:** [ortus-boxlang/bx-esapi](https://github.com/ortus-boxlang/bx-esapi)
**Status:** Documentation and investigation complete; implementation and final verification pending
**Target branch:** `development`

## Problem Statement

### What issue are we solving?

BoxLang's `encodeForURL()` built-in function fails when its optional `canonicalize` flag is set to `true` and the input contains already-encoded URL sequences or characters that ESAPI treats as encoded input.

Reproduction example:

```boxlang
encodeForURL(
    "exam%20p le (fo%2Fo)+,!@#$%^&*()_+~;:.txt",
    true
)
```

The call currently throws an input-validation failure instead of returning a URL-encoded value.

### User impact

- Applications cannot safely use `encodeForURL(value, true)` for filenames, query values, or other user-controlled strings that contain literal percent sequences.
- The failure is surprising because the same function is expected to support the optional canonicalization behavior exposed by the module API.
- The error prevents normal URL encoding and may force callers to add workarounds or disable canonicalization.
- The issue affects the URL encoding path specifically; behavior for the other ESAPI encoding functions must not regress.

### Technical cause

The affected implementation is in [`EncodeForUnits.java`](https://github.com/1234567DN/bx-esapi-issue-3/blob/development/src/main/java/ortus/boxlang/modules/esapi/bifs/EncodeForUnits.java). The method currently performs the following high-level sequence when `canonicalize=true`:

1. Read the input and the optional flag.
2. Canonicalize the input through the ESAPI encoder.
3. Select the requested encoding operation.
4. Call `encoder.encodeForURL(...)` for `encodeForURL`.

Because the input already contains raw percent-encoded sequences such as `%20` and `%2F`, the first step can cause ESAPI's validation/intrusion-detection rules to reject the value before URL encoding gets a chance to process it. The defect is therefore an operation-order problem in the URL-specific path, not a malformed BoxLang call.

> **Important security note:** The fix must preserve ESAPI's security checks and the module's existing behavior for all other encoding contexts. The implementation should change only the URL path required by BL-1119 and should be covered by regression tests.

## Proposed Solution

### What changes will be made and why?

Update the URL encoding flow so that `encodeForURL()` handles the URL-encoding operation in the order required by BL-1119 instead of canonicalizing the raw input first. The implementation should:

1. Keep the existing public signature: `encodeForURL(value, canonicalize=false)`.
2. Isolate the `encodeForURL` branch in `EncodeForUnits.java`.
3. Apply the corrected ESAPI operation order for the URL branch so literal percent sequences are processed without triggering the current premature validation failure.
4. Leave the current canonicalization flow for the other encoding functions unchanged unless a regression test demonstrates that a shared helper must be adjusted.
5. Keep the default `canonicalize=false` behavior unchanged.
6. Add a focused regression test using the BL-1119 reproduction string and at least one control case without percent sequences.

The final implementation should be selected against the expected ESAPI and BoxLang behavior during testing. The regression test must verify both that no validation exception is thrown and that the returned string is URL-encoded correctly.

### Expected behavior

For the reported input, the call should complete successfully:

```boxlang
result = encodeForURL(
    "exam%20p le (fo%2Fo)+,!@#$%^&*()_+~;:.txt",
    true
);
```

Expected results:

- No input-validation or intrusion-detection exception is thrown.
- Spaces and URL-reserved characters are encoded according to ESAPI's URL encoder.
- The method honors `canonicalize=true` according to the corrected URL-specific flow.
- The existing behavior of `encodeForURL(value)` and `encodeForURL(value, false)` remains compatible.

## Implementation Steps

### Step 1 — Set up and understand the issue

- Build the `bx-esapi` module from the `development` branch.
- Run the existing test suite before making changes.
- Reproduce the failure with the exact BL-1119 input.
- Confirm the failure originates in the canonicalization step before `encodeForURL`.
- Review the ESAPI encoder configuration, especially `Encoder.AllowMultipleEncoding` and `Encoder.AllowMixedEncoding`.

### Step 2 — Implement the focused fix

- Update `src/main/java/ortus/boxlang/modules/esapi/bifs/EncodeForUnits.java`.
- Keep the change scoped to `encodeForURL` and the `canonicalize=true` path.
- Avoid changing the public API or the default value of the optional flag.
- Add comments explaining why the URL branch requires a distinct order of operations.
- Preserve existing null handling and behavior for CSS, DN, HTML, HTML attribute, JavaScript, LDAP, XML, XML attribute, XPath, and other supported encoders.

### Step 3 — Add or update regression tests

Add a test in the existing ESAPI BIF test suite, preferably alongside the current tests in:

- `src/test/java/ortus/boxlang/modules/esapi/bifs/ESAPITest.java`
- or a focused `EncodeForUnitsTest.java` if the project conventions support a separate test class.

The test coverage should include:

1. The exact BL-1119 reproduction input with `canonicalize=true`.
2. A URL string with no percent sequences and `canonicalize=true`.
3. The same reproduction input with `canonicalize=false`.
4. The default call with no second argument.
5. At least one neighboring encoder with `canonicalize=true` to detect unintended regressions.
6. A null or empty input case if the existing API contract covers it.

### Step 4 — Run the complete validation suite

- Run the focused regression test first.
- Run all unit and integration tests through the Gradle wrapper.
- Run formatting and static checks required by the repository.
- Review the diff to verify that only the intended source, test, and documentation files changed.

### Step 5 — Review and prepare the contribution

- Compare the implementation against the BL-1119 reproduction and success criteria.
- Update the changelog if the project maintainers require user-facing bug fixes there.
- Open a pull request from the custom repository's `development` branch to the upstream `ortus-boxlang/bx-esapi:development` branch when implementation and tests are complete.
- Include this document or a link to it in the pull request description.

## Validation Plan

### Tests to add or update

| Test area | Test case | Expected result |
|---|---|---|
| Regression | Reported filename/string with `canonicalize=true` | Returns successfully; no validation exception |
| URL encoding | Input containing spaces, `%20`, `%2F`, `+`, punctuation, and reserved characters | Output is encoded according to ESAPI URL rules |
| Default behavior | `encodeForURL(value)` | Existing default behavior is unchanged |
| Explicit false | `encodeForURL(value, false)` | Existing non-canonicalizing behavior is unchanged |
| Neighboring encoders | Representative HTML/XML/JavaScript call with `canonicalize=true` | No unrelated behavior changes |
| Error handling | Invalid or unsupported input covered by current contracts | Existing exception behavior remains intentional |

### Verification steps

1. Start from a clean checkout of the custom repository.
2. Run the baseline test suite and record the result.
3. Reproduce BL-1119 before the fix and capture the failing behavior.
4. Apply the implementation and run the focused regression test.
5. Confirm the exact reproduction string completes successfully.
6. Compare the returned value with the expected URL-encoded representation.
7. Run the complete Gradle test suite.
8. Run formatting/static checks used by CI.
9. Inspect the generated diff and confirm there are no unrelated changes.
10. Record the final test commands and results in the pull request.

### Success criteria

Issue #3 is complete when all of the following are true:

- `encodeForURL(reproductionInput, true)` no longer throws the reported input-validation failure.
- The returned value is correctly URL-encoded and matches the agreed ESAPI behavior.
- Default and explicit `canonicalize=false` behavior remains unchanged.
- Existing tests pass, and the new regression test passes consistently.
- Neighboring encoding functions do not regress.
- CI/build checks pass on the target branch.
- The implementation, tests, and user-facing documentation explain the cause and fix clearly.
- A pull request is ready for upstream review, with the BL-1119 reference included.

## Timeline and Deliverables

| Phase | Week/date | Focus | Deliverable |
|---|---|---|---|
| Setup | Week 5 — September 24 | Set up the environment and become familiar with the issue | Repository fork, local build, reproduction notes |
| Build | Week 6 — October 1 | Work as a team to resolve and test the issue | Initial implementation and focused tests |
| Build | Week 7 — October 8 | First pull request / engineer midpoint check | Draft pull request and midpoint feedback |
| Build | Week 8 — October 15 | Pull request review and ongoing feedback | Updated implementation, tests, and documentation |
| Ship | Week 9 — October 22 | Integrate, test, and document | Final submission and completed validation |
| Ship | Week 10 — October 29 | Project presentation in class | Presentation and final status update |

## Collaboration Links

- **Custom Issue #3 repository:** https://github.com/1234567DN/bx-esapi-issue-3
- **Upstream ESAPI module:** https://github.com/ortus-boxlang/bx-esapi
- **Upstream development branch:** https://github.com/ortus-boxlang/bx-esapi/tree/development
- **Affected source file:** https://github.com/1234567DN/bx-esapi-issue-3/blob/development/src/main/java/ortus/boxlang/modules/esapi/bifs/EncodeForUnits.java
- **Issue documentation:** https://github.com/1234567DN/bx-esapi-issue-3/blob/development/docs/issue-3-canonicalize-encodeforurl.md

## Progress Updates

- **October 15, 2026:** Custom repository created from the upstream `bx-esapi` module. Issue #3 investigation and documentation added. Runtime implementation and regression testing remain the next work items.
- **Completion update:** To be added after the implementation, test suite, and CI checks meet the success criteria above.
