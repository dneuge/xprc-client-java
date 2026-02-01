# Contribution Guidelines

Before contributing code for inclusion to the main repository, please make sure that

- you have read all available documentation of this project
- the source code you are about to commit can be released under MIT license
    - check with project lead before introducing code restricted by licenses
- source code is formatted "correctly", see the section below
- create a pull-request to the main repository for your code (or other file changes) to be reviewed for inclusion to the main project
- when copying (or renaming) files try to remember to first commit a plain file copy without modified content, describe what you copied (from/to) in the commit
  message, and only modify the contents in a later commit
    - the reason is that Git cannot mark files as copies, it can only automatically detect file copies based on content changes which requires a
      user-configurable (thus unpredictable) amount of content to be identical to a previously committed file
- the contribution is your own work and does not introduce copyright issues to the main repository
    - significant portions of code should not be verbatim copies from forums/knowledge databases such as StackOverflow
    - basing a contribution on documentation or other (online) sources is allowed as long as you make sure to abide by the terms of use set by those sources and
      cite/name the original author(s) were applicable
        - when this results in a conflict to other points in these guidelines, the affected contribution needs to be considered as potentially derivative work
          and treated accordingly (note that e.g. including code from an external project does not necessarily violate the "own work" rule, but special care
          must be taken in terms of copyright and license conformity)
        - for any significant "imported" portion of code the **origin must be documented**; attempting to contribute copied code without proper attribution and
          clear indication of sources and licenses is a severe copyright violation
    - **AI must not have been involved in any part of authoring your contribution** because the original sources involved in AI training and thus their authors
      and license conditions are untraceable, bearing a high risk of copyright/license violation
        - you may be **explicitly asked to truthfully pledge not to have or will be using generative AI in any of your contributions**, neither for source code
          nor any other contribution including issue reports or discussions; violation of this strict requirement may lead to permanent removal from the project
          due to a breach of trust and tainting it with legal issues
        - it is highly recommend to disable all AI-related functions in editors you use (e.g. IDEs) while making or preparing contributions to this project to
          avoid any accidental violations of this rule
- all commits are made under your full real name (no aliases, no short names) and with a working email address (it must be possible to contact you under that
  address for the foreseeable future)
- you understand and agree to be listed with that information permanently, unrevokable, as a contributor to the project in the source repository and, at least
  for larger contributions, accompanying documentation
- you understand that such information will be copied and archived permanently in uncontrollable ways due to the nature of services used to collaborate on the
  project, as well as source code repositories and open communication on the Internet in general
- by contributing to the project, including communication, you surrender all rights to be removed from or edited out of the project at any point after your
  contribution has been published (this is required because rights usually granted by laws such as DSGVO/GDPR cannot be fulfilled for technical and legal
  reasons)

## Code formatting

Code formatting should follow general Java style guidelines.

This list only mentions special points that often vary between Java projects:

- use spaces instead of tabs, 4 spaces per indention level
- open code blocks for conditional code, even if it is a one-liner
- do not open code blocks on new lines
- if you have open tasks remaining in your code, mark them with a comment in the format of `// TODO: description`
    - use `FIXME` if you expect potential problems from the code
    - use `TODO` for generally open tasks that do not lead to issues
    - use `DEBUG` on code that can be removed when that level of debugging is no longer required
- as a general rule of thumb, look at existing code if unsure how to format

It is recommended to use [IntelliJ IDEA](https://www.jetbrains.com/idea/)\* as an IDE, import the
[code style included with this project](idea-code-style.xml) and enable *Reformat code* and *Optimize imports* under
*Tools/Actions on Save* for auto-formatting. You may also want to install the "SonarQube for IDE" plugin for static
code analysis.

\*) With commercial versions of IDEA in particular: Remember to disable all AI integrations that may be
active by default or your contribution will be rejected.
