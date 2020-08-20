(require '[clojure.string :as str])

(defn read-files []
   
   (def custMap
		   (reduce
			   (fn [map [custID name address phoneNumber]]
			     (merge map
			            (hash-map (Integer/parseInt custID)
			                      [(Integer/parseInt custID), name, address, (str/trim phoneNumber)])
	             )
	       )
		   {}
		   (map #(str/split % #"\|") (str/split (slurp "cust.txt") #"\n"))
     )
   )
   
   (def prodMap
		   (reduce
		   (fn [map [prodID itemDescription unitCost]]
		     (merge map
		            (hash-map (Integer/parseInt prodID)
		                      [(Integer/parseInt prodID), itemDescription,(Float/parseFloat unitCost) ])))
		   {}
		   (map #(str/split % #"\|") (str/split (slurp "prod.txt") #"\n"))
     )
   )
    (def salesMap
		   (reduce
		   (fn [map [salesID custID prodID itemCount]]
		     (merge map
		            (hash-map (Integer/parseInt salesID)
		                      [(Integer/parseInt salesID),(Integer/parseInt custID) ,(Integer/parseInt prodID), (Integer/parseInt (str/trim itemCount) )])))
		   {}
		   (map #(str/split % #"\|") (str/split (slurp "sales.txt") #"\n"))
     )
   )
)

(defn display [collection]
  
  (if  (not(empty? collection))
    (do
    (println (first (first collection)) ":" [(apply str (flatten [\" (interpose "\",\"" (rest (first collection))) \"]))])
    (display  (rest collection))
    )
    )
  
  )

(defn display-sales [collection]
  (if  (not(empty? collection))
    (do
     (println (get (first collection) 0) ":" [(apply str (flatten [\" (interpose "\",\"" (list (get (get custMap (get (first collection) 1)) 1), (get (get prodMap (get (first collection) 2)) 1), (get (first collection) 3))) \"]))])
       (display-sales  (rest collection))
    )
    )
  )

(defn calc-purchases [custName]
  (def custkey (keep #(when (= (nth (val %) 1) custName) (key %)) custMap))
  (if (not-empty custkey )
    (do
      (def salestot (reduce +(keep #(when (= (nth (val %) 1) (nth custkey 0))
         (* (get (get prodMap (nth (val %) 2)) 2) (nth (val %) 3)))
       salesMap)
         )
        )
      (if (= salestot 0) 
      (println (get (get custMap (nth custkey 0)) 1) ":$" salestot)
      (println (get (get custMap (nth custkey 0)) 1) ":$" (format "%.2f"  salestot ))
      )
      )
     (println custName ":Customer Not Found")
  ) 
)



(defn calc-items [prodName]
  (def prodkey (keep #(when (= (nth (val %) 1) prodName) (key %)) prodMap))
  (if (not-empty prodkey )
  (println prodName ":"
   (reduce + (keep #(when (= (nth (val %) 2) (nth prodkey 0) )
           (nth (val %) 3))
       salesMap))
  )
  (println prodName ":Product Not Found")
  )

  )
(defn main-fn [choice]
  (if (not (= choice "6" )) 
    (do
(println "*** Sales Menu ***
------------------
1. Display Customer Table
2. Display Product Table
3. Display Sales Table
4. Total Sales for Customer
5. Total Count for Product
6. Exit
Enter an option?")
(def x (read-line))
(cond
      (= x "1") (display (sort-by first(vals custMap)))
      (= x "2") (display (sort-by first(vals prodMap)))
      (= x "3") (display-sales (sort-by first(vals salesMap)))
      (= x "4") (do (println "Enter customer name")  (def custName (read-line)) (calc-purchases (str custName)))
      (= x "5") (do (println "Enter product name")  (def prodName (read-line)) (calc-items (str prodName)) )
      (= x "6") (println "Good Bye")
      :else (println "Invalid Input"))
(main-fn (str x)))
    
)
)

(read-files)
(main-fn (str "1"))
