(ns frontend.db.rules)

(def rules
  '[[(parent ?p ?c)
     [?c :block/parent ?p]]
    [(parent ?p ?c)
     [?c :block/parent ?t]
     (parent ?p ?t)]

    [(namespace ?p ?c)
     [?c :block/namespace ?p]]
    [(namespace ?p ?c)
     [?c :block/namespace ?t]
     (namespace ?p ?t)]

    ;; from https://stackoverflow.com/questions/43784258/find-entities-whose-ref-to-many-attribute-contains-all-elements-of-input
    ;; Quote:
    ;; You're tackling the general problem of 'dynamic conjunction' in Datomic's Datalog.
    ;; Write a dynamic Datalog query which uses 2 negations and 1 disjunction or a recursive rule
    ;; Datalog has no direct way of expressing dynamic conjunction (logical AND / 'for all ...' / set intersection).
    ;; However, you can achieve it in pure Datalog by combining one disjunction
    ;; (logical OR / 'exists ...' / set union) and two negations, i.e
    ;; (For all ?g in ?Gs p(?e,?g)) <=> NOT(Exists ?g in ?Gs, such that NOT(p(?e, ?g)))

    ;; [(matches-all ?e ?a ?vs)
    ;;  [(first ?vs) ?v0]
    ;;  [?e ?a ?v0]
    ;;  (not-join [?e ?vs]
    ;;            [(identity ?vs) [?v ...]]
    ;;            (not-join [?e ?v]
    ;;                      [?e ?a ?v]))]
    ])

(def query-dsl-rules
  "Rules used by frontend.db.query-dsl. The symbols ?b and ?p respectively refer
  to block and page. Do not alter them as they are programatically built by the
  query-dsl ns"
  {:page-property
   '[(page-property ?p ?key ?val)
     [?p :block/name]
     [?p :block/properties ?prop]
     [(get ?prop ?key) ?v]
     (or [(= ?v ?val)] [(contains? ?v ?val)])]

   :has-page-property
   '[(has-page-property ?p ?key)
     [?p :block/name]
     [?p :block/properties ?prop]
     [(get ?prop ?key)]]

   :task
   '[(task ?b ?markers)
     [?b :block/marker ?marker]
     [(contains? ?markers ?marker)]]

   :priority
   '[(priority ?b ?priorities)
     [?b :block/priority ?priority]
     [(contains? ?priorities ?priority)]]

   :page-tags
   '[(page-tags ?p ?tags)
     [?p :block/tags ?t]
     [?t :block/name ?tag]
     [(contains? ?tags ?tag)]]

   :all-page-tags
   '[(all-page-tags ?p)
     [?e :block/tags ?p]]

   :between
   '[(between ?b ?start ?end)
     [?b :block/page ?p]
     [?p :block/journal? true]
     [?p :block/journal-day ?d]
     [(>= ?d ?start)]
     [(<= ?d ?end)]]

   :has-property
   '[(has-property ?b ?prop)
     [?b :block/properties ?bp]
     [(missing? $ ?b :block/name)]
     [(get ?bp ?prop)]]

   :block-content
   '[(block-content ?b ?query)
     [?b :block/content ?content]
     [(clojure.string/includes? ?content ?query)]]

   :page
   '[(page ?b ?page-name)
     [?b :block/page [:block/name ?page-name]]]

   :namespace
   '[(namespace ?p ?namespace)
     [?p :block/namespace ?parent]
     [?parent :block/name ?namespace]]

   :property
   '[(property ?b ?key ?val)
     [?b :block/properties ?prop]
     [(missing? $ ?b :block/name)]
     [(get ?prop ?key) ?v]
     (or-join [?v]
              [(= ?v ?val)]
              [(contains? ?v ?val)]
              ;; For integer pages that aren't strings
              (and
               [(str ?val) ?str-val]
               [(contains? ?v ?str-val)]))]

   :page-ref
   '[(page-ref ?b ?page-name)
     [?b :block/path-refs [:block/name ?page-name]]]})
