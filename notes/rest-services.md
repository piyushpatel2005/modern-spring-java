# Spring REST services

Spring MVC supports annotations for various types of HTTP requests. All REST API end points are created inside `taco.web.api` package. To enable paging in the REST api, TacoRepository extends `PagingAndSortingRepository` instead of `CrudRepository`.