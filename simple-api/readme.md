Simple Api
----------

Groovy Load Script

``` groovy 
10.times { thread ->
    Thread.start {
        500.times { loop ->
            Thread.sleep((long) (Math.random() * 100));
            "http://localhost:8282/".toURL().text
        }
    }

    Thread.start {
        500.times { loop ->
            Thread.sleep((long) (Math.random() * 100));
            "http://localhost:8282/application/slow".toURL().text
        }
    }

    Thread.start {
        5000.times { loop ->
            Thread.sleep((long) (Math.random() * 100));
            "http://localhost:8282/application/fast".toURL().text
        }
    }
}

return
```