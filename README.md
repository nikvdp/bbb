# `bbb` - BabashkaBins

## The problem
Been learning Clojure lately and enjoying it greatly, but one thing I miss from
Golang is how great the build and deploy cycle for is for building CLI tools.
Once your project is set up just type a quick `go run` or `go build` and you
can instantly run your app or get an easy to deploy single binary that will
run anywhere.

On top of it, there are great libraries like
[viper](https://github.com/spf13/cobra) that make building CLI tools with
subcommands and complicated parsing easy and fast. 

Getting to that level of integration in Clojure is hard since Clojure is a
hosted language and needs to run on the JVM, but with GraalVM and babashka it
turns out you can get pretty close. This often doesn't matter in Clojure, bec

## The solution
`bbb` is my attempt to get a similar workflow for Clojure. Goals:

- An Easy and fast dev experience. 
  
  You should be able to re-run your CLI immediately and see any changes, no
  waiting for compliation or waiting 2 seconds for the JVM to launch
- Easy command-line parsing, with ergonomic support for infinitely nested
  subcommands and long and short flags + help texts.
- A good deploy story. 

  Once you're app is ready to distribute you should be able to create a single
  fast-starting static binary that's as easy to use for your end-users as a
  Golang binary built with `go build` would be

This repo combines babashka, cli-matic, and GraalVM to get something pretty close to the above:

- Use `babashka` during development for a quick iteration cycle and fast startup times
- Use `cli-matic` to define the CLI parsing logic, and be sure it will work the
  same in your `bb` testing and in the compiled static binary 
- compile to a static binary through GralVM when ready, without having to spend
  hours learning how to tweak GraalVM or finding out that some feature that
  works fine in JVM Clojure is not compatible with GraalVM


## Prereqs
- [babashka][1]
- `clojure` / `clj` cli
- [GraalVM][3] and it's `native-image` installed via `gu` 

## Usage

1. Clone this repo, and run `git submodule update --init --recursive` inside it
2. Add your code under the `src/` folder using the standard Clojure folder
   structure, using a namespace and a `-main` function
3. Edit `deps.edn` and change the "example.core" under (get-in [:aliases
   :native-image :main-opts]) to use your project's namespace
3. Require bbb.core from your project's namespace (add `(:require [bbb.core])`
   to your `ns` macro)
4. To run your project, use `bb -m <your-namespace>`
5. To compile your project, do `clj -A:native-image`, and your project will be
   compiled to `bb`.

   You can also compile a different namesapce by doing `clj -A:native-image '{:main-ns "some.otherns"}`


### run
`clojure`

### build native image:
```
clj -A:native-image
```

[1]: https://github.com/borkdude/babashka
[2]: https:// 
[3]: https://
