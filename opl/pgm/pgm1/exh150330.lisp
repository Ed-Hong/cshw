; Author: Edward Hong
; CE 4337.003
; Feb 13, 2019

(defun divisible-by-7 (x)
    (if (= 0 (mod x 7))
        t
    nil)
)

(defun function-3 (f)
    (funcall f 3)
)

(defun zipper (a b)
    (if (and a b)
        (cons (cons (car a) (list(car b))) (zipper (cdr a) (cdr b)))
    )
)

(defun my-map (f l)    
    (if l 
        (cons (funcall f (car l)) (my-map f (cdr l)))
    )
)

(defun get-evens (l)
    (if l
        (if (= 0 (mod (car l) 2))
	    (cons (car l) (get-evens (cdr l))) 
	(get-evens (cdr l)))
    )
)

(defun get-odds (l)
    (if l
        (if (not (= 0 (mod (car l) 2)))
	    (cons (car l) (get-odds (cdr l)))
	(get-odds (cdr l)))
    )
)

(defun segregate (l)
    (cons (get-evens l) (cons (get-odds l) nil))
)
