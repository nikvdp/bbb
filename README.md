# BabashkaBins

A low friction, quick and easy way to develop CLI tools in Clojure that
you can distribute as self-contained static binaries (babashka-bins).

`bbb` lets you take a standard Clojure project layout, run it under both JVM
Clojure and [babashka](https://github.com/babashka/babashka), and then
automates the compilation of your project into a static binary with GraalVM for
you when it's time to distribute it.

## The problem

Clojure is great, but building and deploying CLI tools with Clojure is painful enough that hardly anyone does it, which is a pity.

There are two main problems with building CLI tools in Clojure:

- GraalVM is an incredible technical achievement, but it’s hard to use, slow, and finicky. Some programs that work fine in JVM Clojure cannot be compiled w/GraalVM or can only be compiled after mucking with reflection configurations and other unpleasantries. Worse yet, you often don’t find out that your code doesn’t compile until you’ve waited through a ([potentially >15m](https://twitter.com/ArghZero/status/1480215787994775552)) GraalVM compile cycle
- Developing Clojure CLI applications is cumbersome because you have to wait for a full JVM spin-up for each run. It’s only 1-2 seconds, but it adds up quickly

This project aims to solve both of these by making it really easy to build CLI tools in Clojure that will definitely compile under GraalVM with no extra brain cells required on your part.

## The solution

A good solution for this problem should have the following properties:

- **An easy and fast dev experience**: You should be able to re-run your CLI immediately and see any changes, no waiting for compilation or waiting 2 seconds for the JVM to launch
- **Easy command-line parsing**: ergonomic support for infinitely nested subcommands and long and short flags + help texts.
- **A good deploy story**: once your app is ready to distribute it should be easy to create a single fast-starting static binary that you can deploy to your users without asking them to install any other software.

Babashka and cli-matic to the rescue!

[Babashka](https://github.com/borkdude/babashka) is a Clojure interpreter compiled under [GraalVM](https://www.graalvm.org/). Since Babashka itself is compiled with GraalVM, anything that runs in babashka will by definition also work under GraalVM. While we can’t make GraalVM more compatible, we can at least find out that a certain library or approach won’t work *early*.


[cli-matic][cli-matic] is an easy to use library for ergonomically parsing command-line arguments and building complex CLI tools, even with nested subcommands, something that can be quite tricky with the standard Clojure CLI parsing toolkit. [cli-matic][cli-matic] can run under babashka, but it requires some [pretty intense hackery](https://github.com/borkdude/spartan.spec/blob/master/examples/cli_matic.clj#L1-L19). This project provides a standardized interface to [cli-matic][] that works the same way, regardless of whether it’s called from JVM Clojure or babashka.

With these tools together, this project get you to something pretty close to the ideal solution above:

- Use [`babashka`][babashka] during development for a quick iteration cycle and
  fast startup times
- Use [`cli-matic`][cli-matic] to define the CLI parsing logic, and be
  confident that it will work the same way in the compiled static binary
- Automatically compile your project into a static binary with GraalVM. No need
  to spend hours learning how to tweak Graal or finding out that some feature
  that works fine in JVM Clojure is not compatible with GraalVM

## Prerequisites

- [`babashka`](https://github.com/borkdude/babashka)
- [`clojure`](https://clojure.org/guides/getting_started) / [`clj`](https://clojure.org/guides/getting_started) cli
- [GraalVM](https://www.graalvm.org/) and it’s `native-image` component installed via `gu`. You can install from their website, or if you trust my `bash`-ing, try the below: 
    
  <details><summary>GraalVM CLI Installation instructions</summary>
  <p>

  - To start, paste the following into a terminal:
    ```bash
    install-graalvm() {
      local platform="$(echo "$OS_PLATFORM" | tr '[:upper:]' '[:lower:]')"
    
      wget -O "/tmp/graalvm.tar.gz" "https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-21.1.0/graalvm-ce-java11-$platform-amd64-21.1.0.tar.gz"
      # cp /tmp/graalvm.tar.gz.bak /tmp/graalvm.tar.gz # for testing
    
      mkdir -p /tmp/graalvm/out
      tar -C /tmp/graalvm/out -xvzf /tmp/graalvm.tar.gz
      [[ -d ~/graalvm ]] && mv ~/graalvm ~/graalvm.bak
      mv /tmp/graalvm/out/graalvm-ce-* ~/graalvm
    
      # install native-image binary
      if [[ -d ~/graalvm/bin ]]; then
        ~/graalvm/bin/gu install native-image
      elif [[ -d ~/graalvm/Contents/Home/bin ]]; then # handle macos folder structure
        ~/graalvm/Contents/Home/bin/gu install native-image
      fi
    
      # cleanup
      rm /tmp/graalvm.tar.gz
    }
    install-graalvm
    ```
    
  - The above will install GraalVM for you. To activate it and make it
    permanent, you'll also need to add the below to your `~/.bashrc`/`~/.zshrc`
    file and start a new terminal session. 
    
    ```bash
    graalvm-setup() {
      if [[ "$CUR_PLATFORM" == "Darwin" ]]; then
        local graal_home=
        if [[ -d /Library/Java/JavaVirtualMachines/graalvm-ce-*/Contents/Home ]]; then
          graal_home=(/Library/Java/JavaVirtualMachines/graalvm-ce-*/Contents/Home)
        fi
        if [[ -d "$graal_home" ]]; then
          export GRAALVM_HOME="$graal_home"
        fi
      fi
      if [[ -d ~/graalvm/bin ]]; then
        export GRAALVM_HOME="$HOME/graalvm"
      elif [[ -d ~/graalvm/Contents/Home/bin ]]; then
        export GRAALVM_HOME="$HOME/graalvm/Contents/Home"
      fi
      if [[ -n "$GRAALVM_HOME" ]]; then
        add-to-path "$GRAALVM_HOME/bin"
      fi
    }
    graalvm-setup
    ```
  </details>
    

## Usage

### Prepare your project

1. Clone this repo
2. Add your code under the `src/` folder using the standard Clojure folder
   structure, and make sure the namesapce you'll be using as your app's entrypoint has a `-main` function.
    - If you plan to use [`cli-matic`][cli-matic] (recommended) to parse your
      CLI options, require `run-cmd` from `bbb.core` (see `example.core` for
      an example)
    - **Make sure to add `(:gen-class)` to your namespace's `(ns)` macro** to prevent
      head-scratch inducing GraalVM related issues later!
3. Edit `deps.edn` and change the `:main-ns` key near the top of the file
   (under `:aliases`→`:native-image`→`:exec-args`→`:main-ns`) from
   `example.core` to your your project’s namespace
4. To run your project, use `bb -m <your-namespace>`
5. To compile your project, do `clj -A:native-image`, and your static binary
   will be put in the project root of the folder with the name `bb`. (You can
   also compile a different namespace by doing `clj -A:native-image '{:main-ns
   "some.otherns"}`)
    

### Running your project (dev mode)

You can run your project with babashka at any time by doing the following (replacing `example.core` with the name of your own namespace if you’ve created one)

```
bb -m example.core
```

Any additional command-line parameters will be passed to your project for parsing by cli-matic:

```bash
$ bb -m example.core so many args --example cool
I was called as an example with args:
({:example cool, :_arguments [so many args]})
```

If you want to verify that you haven’t broken JVM Clojure compatibility you can also run the equivalent JVM Clojure command:

```bash
clj -m example.core
```

### Compiling your project to a static binary

If you have GraalVM and it's `native-image` tool installed and configured,
thanks to
[clj.native-image](https://github.com/taylorwood/clj.native-image.git)
compiling your project is as simple as:

```
clj -X:native-image
```

This will compile your project based on the namespace set in `deps.edn`'s `:main-ns` key (under `:aliases` → `:native-image` → `:exec-args` → `:main-ns`).

You can also override the `:main-ns` configured in `deps.edn` from the command line:

```bash
clj -X:native-image '{:main-ns "some-other-namespace.core"}'
```

The static binary will end up in your root folder with the name `bb`.

### Adding dependencies

You can add maven dependencies to your `deps.edn` file in the same way as you would for any other tools.deps-based project. Not all libraries will work correctly under babashka/GraalVM, but adding them to `deps.edn` will give you the chance to quickly find out if the library you’re interested in will work or not.

<!-- 
just some handy vim macros

yst ]f]a[cli-matic
yst ]f]a[babashka
-->

[babashka]: https://github.com/borkdude/babashka
[clj]: https://clojure.org/guides/getting_started 
[cli-matic]: https://github.com/l3nz/cli-matic
[graalvm]: https://www.graalvm.org/

