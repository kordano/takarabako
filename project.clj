(defproject takarabako "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [http-kit "2.1.18"]
                 [compojure "1.4.0"]
                 [org.omcljs/om "1.0.0-alpha14"]
                 [io.replikativ/konserve "0.3.0-beta3"]
                 [com.cemerick/piggieback "0.2.1"]
                 [org.clojure/tools.nrepl "0.2.11"]
                 [jarohen/chord "0.6.0"]]
  :source-paths ["src/cljs" "src/clj"]
  :plugins [[lein-figwheel "0.4.1"]
            [lein-cljsbuild "1.1.0"]]
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  :figwheel {:nrepl-port 7888
             :css-dirs ["resources/public/css"]}
  :clean-targets ^{:protect false} ["resources/public/js" "target" "out"]
  :cljsbuild {:builds
              {:dev
               {:source-paths ["src/cljs"]
                :figwheel true
                :compiler {:main takarabako.core
                           :output-to "resources/public/js/main.js"
                           :output-dir "resources/public/js/out"
                           :asset-path "js/out"
                           :optimizations :none}}
               :test
               {:source-paths ["test/cljs"]
                :compiler {:output-to "resources/public/js/test/main.js"
                           :main takarabako.test-runner
                           :optimizations :none}}
               :release
               {:source-paths ["src/cljs"]
                :compiler {:main takarabako.core
                           :output-to "resources/public/js/main.js"
                           :cache-analysis true
                           :optimizations :advanced}}}})
