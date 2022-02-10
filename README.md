# BabashkaBins

A low friction, quick and easy way to develop CLI tools in Clojure that you can distribute as self-contained static binaries (babashka-bins) for macOS and Linux (windows support coming eventually).

`bbb` lets you take a standard Clojure project layout, run it under both JVM Clojure and [babashka](https://github.com/babashka/babashka), and then automates the compilation of your project into a static binary with GraalVM for you when it’s time to distribute it.

## The problem

Clojure is great, but building and deploying CLI tools with Clojure is painful enough that hardly anyone does it, which is a pity.

There are three main problems with building CLI tools in Clojure:

- Developing Clojure CLI applications is cumbersome because you have to wait for a full JVM spin-up for each run. It’s only 1-2 seconds, but it adds up quickly
- Distributing Clojure CLI application is cumbersome because now your users have to a) have java installed, and b) have to wait 1-2 seconds each time they run your app
- GraalVM, the potential solution to the above, is an incredible technical achievement, but it’s hard to use, slow, and finicky. Some programs that work fine in JVM Clojure cannot be compiled w/GraalVM or can only be compiled after mucking with reflection configurations and other unpleasantries. Worse yet, you often don’t find out that your code doesn’t compile until you’ve waited through a ([potentially >15m](https://twitter.com/ArghZero/status/1480215787994775552)) GraalVM compile cycle

This project aims to solve these problems by making it really easy to build CLI tools in Clojure that will definitely compile under GraalVM with no extra brain cells required on your part.

## The solution

A good solution for this problem should have the following properties:

- **An easy and fast dev experience**: You should be able to re-run your CLI immediately and see any changes, no waiting for compilation or waiting 2 seconds for the JVM to launch
- **Easy command-line parsing**: ergonomic support for infinitely nested subcommands and long and short flags + help texts.
- **A good deploy story**: once your app is ready to distribute it should be easy to create a single fast-starting static binary that you can deploy to your users without asking them to install any other software.

Babashka and cli-matic to the rescue!

[Babashka](https://github.com/borkdude/babashka) is a Clojure interpreter compiled under [GraalVM](https://www.graalvm.org/). Since Babashka itself is compiled with GraalVM, anything that runs in babashka will by definition also work under GraalVM. While we can’t make GraalVM more compatible, we can at least find out that a certain library or approach won’t work *early*.

[cli-matic](https://github.com/l3nz/cli-matic) is an easy to use library for ergonomically parsing command-line arguments and building complex CLI tools, even with nested subcommands, something that can be quite tricky with the standard Clojure CLI parsing toolkit. [cli-matic](https://github.com/l3nz/cli-matic) can run under babashka, but requires some [pretty intense hackery](https://github.com/borkdude/spartan.spec/blob/master/examples/cli_matic.clj#L1-L19). This project provides a standardized interface to [cli-matic](https://github.com/l3nz/cli-matic) that works the same way, regardless of whether it’s called from JVM Clojure or babashka.

With these tools together, this project can get you pretty close to the good solution above:

- Use [`babashka`](https://github.com/borkdude/babashka) during development for a quick iteration cycle and fast startup times
- Use [`cli-matic`](https://github.com/l3nz/cli-matic) to define the CLI parsing logic, and be confident that it will work the same way in the compiled static binary
- Automatically compile your project into a static binary with GraalVM. No need to spend hours learning how to tweak Graal or finding out that some feature that works fine in JVM Clojure is not compatible with GraalVM

## Prerequisites

- [`babashka`](https://github.com/borkdude/babashka)
- The [`clojure`](https://clojure.org/guides/getting_started) and [`clj`](https://clojure.org/guides/getting_started) cli tools
- (optional) [GraalVM](https://www.graalvm.org/) and it’s `native-image` component installed via `gu`. 

If `bbb` can’t find a system-wide GraalVM installation it will attempt to download one into the `vendor/` folder for you and use that. **This is experimental** and requires that you have `wget` installed.

## Usage

### Prepare your project

1. Clone this repo
2. Run `git submodule update --init --recursive` to pull in babashka’s source.
3. Add your code under the `src/` folder using the standard Clojure folder structure, and make sure the namespace you’ll be using as your app’s entrypoint has a `-main` function.
    - If you plan to use [`cli-matic`](https://github.com/l3nz/cli-matic) (recommended) to parse your CLI options, require `run-cmd` from `bbb.core` (see `example.core` for an example)
    - **Make sure to add `(:gen-class)` to your namespace’s `(ns)` macro** to prevent head-scratch inducing GraalVM related issues later!
4. Edit `bb.edn` and change the `MAIN-NS` declaration at the top to point to your own namespace (it’s set to `example.core` by default)

### Running your project (dev mode)

You can run your project with babashka at any time by doing the following:

```
bb run
```

Any additional command-line parameters will be passed to your project for parsing by cli-matic:

```
$ bb run so many args --example cool
I was called as an example with args:({:example cool, :_arguments [so many args]})
```

If you want to verify that you haven’t broken JVM Clojure compatibility you can also run the project with JVM Clojure:

```
bb run-clj
```

### Compiling your project to a static binary

Thanks to [clj.native-image](https://github.com/taylorwood/clj.native-image.git) compiling your project is as simple as:

```
bb native-image
```

For reasons, the static binary will end up in your root folder with the name `bb` (I will be working on making this customizable in the future).

### Adding dependencies

You can add maven dependencies to your `deps.edn` file in the same way as you would for any other tools.deps-based project. Not all libraries will work correctly under babashka/GraalVM, but adding them to `deps.edn` will give you the chance to quickly find out if the library you’re interested in will work or not.

## Limitations

- Currently GraalVM on macOS only supports Intel processors, so you can’t compile native arm64/Apple Silicon CLIs. In practice this is usually fine since the Intel versions will run under Rosetta 2 though.
- No Windows support (for now at least, though this approach should theoretically work on Windows too)
- No cross-compilation support, to compile a macOS binary you’ll need a Mac and to compile a Linux binary you’ll need a Linux box.

## Further work

- [ ] Add a Github action template to the repo to make it easier to compile for multiple platforms
- [ ] Figure out how to customize the name of the emitted binary. Normally GraalVM allows you to pass an `-H:Name=` flag, but for reasons I don’t yet understand babashka’s own `-H:Name=` flag is taking precedence.
- [ ] Create a more comprehensive example illustrating how to do cool cli-matic-y things (eg subcommands)
- [ ] Find a better solution to importing the babashka namespaces and GraalVM fixes (these were provided by [@borkdude](https://github.com/borkdude) [here](https://twitter.com/borkdude/status/1480464513434537985?s=20&t=6Thavc6OjTjclYJ9RQEWyQ)) than git submodules.

## Contributing / How to help

I’m pretty new to the world of Clojure and there are probably many things that could be done better. Pull requests and feedback welcome!

If you like this project and would like to see more work like this, feel free to sponsor me on Github or come say hello on Twitter ([@arghzero](https://twitter.com/arghzero))

## Credits / Thanks

This project depends on the following fantastic projects to do it’s work:

- [borkdude/babashka](https://github.com/borkdude/babashka)
- [l3nz/cli-matic](https://github.com/l3nz/cli-matic)
- [taylorwood/clj.native-image](https://github.com/taylorwood/clj.native-image.git)
- [GraalVM](https://www.graalvm.org)
