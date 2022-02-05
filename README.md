# BabashkaBin

This repo is an easy to use template to allow you to get the holy grail of Clojure CLI tooling development:

- Develop w/`babashka` for a quick iteration cycle, compile to a static binary when ready.


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
4. To run your project, use `bb -m <your-namespace>`
5. To compile your project, do `clj -A:native-image`


### run
`clojure`

### build native image:
```
clj -A:native-image
```

[1]: https://github.com/borkdude/babashka
[2]: https:// 
[3]: https://
