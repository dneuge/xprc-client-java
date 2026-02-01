# Native Java Client for XPRC (X-Plane Remote Control)

[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE.md)
[![Issue Tracking: Codeberg](https://img.shields.io/badge/issue%20tracking-codeberg-2684cf)](https://codeberg.org/dneuge/xprc-client-java/issues)

This is a native Java client implementation to connect to and interact with XPRC.

XPRC provides an easy way to interact with X-Plane datarefs and commands from other applications/computers via a
TCP network connection.

Related projects:

- [reference server implementation in C](https://github.com/dneuge/xprc-server-plugin-c) (X-Plane plugin, available on
  [Codeberg](https://codeberg.org/dneuge/xprc-server-plugin-c) and [GitHub](https://github.com/dneuge/xprc-server-plugin-c))
- the [protocol specification](https://codeberg.org/dneuge/xprc-protocol) (available on [Codeberg](https://codeberg.org/dneuge/xprc-protocol)
  and [GitHub](https://github.com/dneuge/xprc-protocol))

Official repositories are hosted on [Codeberg](https://codeberg.org/dneuge/xprc-client-java) and
[GitHub](https://github.com/dneuge/xprc-client-java).
Both locations are kept in sync and can be used to submit pull requests but issues are only tracked
on [Codeberg](https://codeberg.org/dneuge/xprc-client-java/issues) to gather them in a single place.
Please note that this project has a strict "no AI" policy [affecting all contributions](CONTRIBUTING.md) incl.
issue reports.

## Current State

Functionally, the client is in a stable, working state.

The [protocol](https://codeberg.org/dneuge/xprc-protocol) is not stable yet, meaning today's versions of this
client implementation may be incompatible with future protocol/server revisions.

The API still requires cleanup before a first release can be made. In particular, command classes currently use a large
number of type parameters to achieve a fluent API. There sure is a better way to do that but that cleanup has been
post-poned for time-constraints; getting the project (and associated projects) going had higher priority up until now.
It is recommended to use raw types (omit generics) when having to work with command classes (suffixed `CommandBuilder`,
`Channel`, `Message` in the `commands` package) instead of specifying any type parameters as those will still be
reworked.

## Requirements

**This library requires at least Java 8.** Being a library, this project aims to be as compatible as possible without
restricting users to specific JDK releases unless there are good reasons. Java 8 will probably continue to be supported
until our recommended free OpenJDK™ distribution Eclipse Temurin™ ends their [JDK 8 support](https://adoptium.net/support)
around December 2030.

### Note on (not) handling `null` values

This library should be expected to be **unsafe** in terms of handling `null` values as it follows the policy of avoiding
`null` in favor of `Optional`s for return values and more specific (overloaded) method signatures in favor of having
null-checks all across the code:

- never provide `null` to the library (may be unhandled, causing a `NullPointerException`)
- in turn, the library will never return `null` but `Optional`s where a value may be missing
- if you can't provide a value to a method, check for overloaded signatures
- builders may perform deferred checks for required information during construction

## License

All sources and original files of this project are provided under [MIT license](LICENSE.md), unless declared otherwise
(e.g. by source code comments). Please be aware that dependencies (e.g. libraries and/or external data used by this
project) are subject to their own respective licenses which can affect distribution, particularly in binary/packaged
form.

### Note on the use of/for AI

Usage for AI training is subject to individual source licenses, there is no exception. This generally means that proper
attribution must be given and disclaimers may need to be retained when reproducing relevant portions of training data.
When incorporating source code, AI models generally become derived projects. As such, they remain subject to the
requirements set out by individual licenses associated with the input used during training. When in doubt, all files
shall be regarded as proprietary until clarified.

Unless you can comply with the licenses of this project you obviously are not permitted to use it for your AI training
set. Although it may not be required by those licenses, you are additionally asked to make your AI model publicly
available under an open license and for free, to play fair and contribute back to the open community you take from.

AI tools are not permitted to be used for contributions to this project. The main reason is that, as of time of writing,
no tool/model offers traceability nor can today's AI models understand and reason about what they are actually doing.
Apart from potential copyright/license violations the quality of AI output is doubtful and generally requires more
effort to be reviewed and cleaned/fixed than actually contributing original work. Contributors will be asked to confirm
and permanently record compliance with these guidelines.

## Acknowledgements

Java and OpenJDK are trademarks or registered trademarks of Oracle and/or its affiliates.

X-Plane is a registered trademark of Austin Meyer and Aerosoft.
