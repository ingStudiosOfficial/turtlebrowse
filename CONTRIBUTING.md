# Contribute to Turtlebrowse

Thank you for contributing to Turtlebrowse. We welcome any contributions that are **human made**. Please follow our contributing guidelines before making a pull request.

## Use of AI

Turtlebrowse **strictly prohibits** the use of AI in our codebase.
We do this to ensure that the code is readable, maintainable, and of quality that AI codebases usually are not.

**Unacceptable use of AI includes:**

- Completely AI generated code (e.g. the whole file is written by AI, no human involvement)
- Chunks of AI generated code (e.g. a section of the codebase was written by AI)
- AI generated refactored human code (e.g. you wrote the code by yourself but AI refactored it)
- AI generated text, paragraph (e.g. sentences/phrases in strings, text in markdown etc.)
- IDE autocompletions (e.g. VS Code autocomplete)
- Using AI to generate pull request descriptions (e.g. rephrasing of your pull request, completely generating your pull request etc.)

This list is not exhaustive. We reserve the right to reject any code that we suspect might be AI generated.

If you submit AI generated code, you will be banned from contributing to this repository again.

## Styling Guidelines

- For general formatting, follow `.editorconfig` at the root of the project and configure your IDE to use it
- For Java, follow the `java_style_guide.xml` at the root of the project and configure your IDE to use it
- For Go, use the defualt Go styling guidelines and configure your IDE to use it
- For Vue/TypeScript, use the `.oxfmtrc.json` in each web directory
- Adhere to the [Material 3 foundations](https://m3.material.io/foundations) in building UI

## Commit Message Style

We follow [Conventional Commits](https://www.conventionalcommits.org/).

Examples of this include:

- feat: [your new feature here]
- fix: [your bug fix here]
- chore: [chore here (e.g. version bumps, changelogs)]
- style: [description of your code formatted]
- perf: [your performance improvement here]
- refactor: [your refactor here]

You are not required to add the scope to the commit message, but it is much apprecitated if you do (e.g. feat(website): fetch latest version from update microservice)

## Branch Guidelines

When you fork this repository, you should name your branch as such:

`development/brief-feature-description`

## Restrictions

**Please do not:**

- Bump the version or add features in `app/build.gradle.kts` or `CHANGELOG.md` as they are for the core maintainers to change
- Modify GitHub workflows

These pull requests will be rejected.

## Features per Pull Request

Please limit the scope of your pull request to **ONE feature, bug fix, or refactor etc.** per pull request.

## Contributor Etiqutte

- We reserve the right to reject any pull request with no reason
- Do not keep mentioning the contributors if we repeatedly do not accept your pull request - you'll be banned from contributing further
- Be nice when interacting with the community

## Quick Setup

1. **Fork the repository**

Fork this repository via GitHub.

2. **Clone the repository**
```bash
git clone https://github.com/[YOUR_GITHUB_USERNAME]/turtlebrowse.git # or whatever your repo URL is
cd turtlebrowse
```

3. **Create a development branch**
```bash
git switch -c development/brief-feature-description
```

4. **Full build script**
```bash
# Relative to the root of the project
./full-build.sh
```

Detailed build instructions can be found in the [README](./README.md#development).

Thanks for contributing to Turtlebrowse!

© 2026 (ing) Studios and Ethan Lee
