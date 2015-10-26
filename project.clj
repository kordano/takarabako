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
                 [org.omcljs/om "0.9.0"]
                 [kioo "0.4.1"]
                 [jarohen/chord "0.6.0"]
                 [secretary "1.2.3"]
                 [com.taoensso/carmine "2.12.0"]]
  :source-paths ["src/cljs" "src/clj"]
  :profiles {:dev
             {:dependencies [[midje "1.6.3" :exclusions [org.clojure/clojure]]
                             [cljsjs/react-with-addons "0.13.3-0"]
                             [com.cemerick/piggieback "0.2.1"]
                             [org.clojure/tools.nrepl "0.2.11"]
                             [cljs-react-test "0.1.3-SNAPSHOT"]]
              :plugins [[lein-midje "3.1.3"]
                        [lein-figwheel "0.4.1"]
                        [cljs-react-test "0.1.3-SNAPSHOT"]
                        [lein-doo "0.1.5"]]}
             :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
             :figwheel {:nrepl-port 7888
                        :css-dirs ["resources/public/css"]}}
  :plugins [[lein-cljsbuild "1.1.0"]]
  :clean-targets ^{:protect false} ["resources/public/js" "target" "out"]
  :cljsbuild {:builds
              {:dev
               {:source-paths ["src/cljs"]
                :figwheel true
                :compiler {:main takarabako.core
                           :output-to "resources/public/js/main.js"
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
