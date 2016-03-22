(ns sv.gcloud.metadata
  (:require [clj-http.client :as c]
            [clojure.edn :as edn]
            [clojure.core.memoize :as m]))

(def default-config
  {:base-url "http://metadata.google.internal/computeMetadata/v1"
   :parse edn/read-string})

(defn build-request [params]
  {:method :get
   :url (str (:base-url params)
             (:path params))
   :headers {"Metadata-Flavor" "Google"}})

(defn extract-metadata [params response]
  (try
    {:data ((:parse params) (:body response))
     :etag (get-in response [:headers "ETag"])}
    (catch Exception e
      (ex-info "can not extract metadata"
               {:params params}
               e))))

(defn get-metadata [params]
  (let [params (merge default-config params)]
    (extract-metadata params (c/request (build-request params)))))

(def cached-get-metadata
  (m/ttl get-metadata :ttl/threshold (* 1000 60)))
