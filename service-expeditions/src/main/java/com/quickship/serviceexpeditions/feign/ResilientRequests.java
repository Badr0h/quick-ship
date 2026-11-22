package com.quickship.serviceexpeditions.feign;

import com.quickship.serviceexpeditions.exception.ServiceExterneIndisponibleException;
import feign.FeignException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class ResilientRequests {
    private final ClientsRequest clientsRequest;
    private final ReseauRequest reseauRequest;
    private final CacheManager cacheManager;

    public ResilientRequests(ClientsRequest clientsRequest, ReseauRequest reseauRequest, CacheManager cacheManager) {
        this.clientsRequest = clientsRequest;
        this.reseauRequest = reseauRequest;
        this.cacheManager = cacheManager;
    }

    @Retry(name = "service-clients", fallbackMethod = "customerDeRepli")
    @CircuitBreaker(name = "service-clients")
    @Bulkhead(name = "service-clients")
    public CustomerDto getCustomerByIdFb(Long id) {
        CustomerDto c = clientsRequest.getCustomerById(id);
        mettreEnCache("customers", id, c);
        return c;
    }

    @Retry(name = "service-reseau", fallbackMethod = "routeDeRepli")
    @CircuitBreaker(name = "service-reseau")
    @Bulkhead(name = "service-reseau")
    public RouteDto getRouteByIdFb(Long id) {
        RouteDto r = reseauRequest.getRouteById(id);
        mettreEnCache("routes", id, r);
        return r;
    }

    @Retry(name = "service-clients", fallbackMethod = "customerIndisponible")
    @CircuitBreaker(name = "service-clients")
    @Bulkhead(name = "service-clients")
    public CustomerDto getCustomerByIdStrict(Long id) {
        CustomerDto c = clientsRequest.getCustomerById(id);
        mettreEnCache("customers", id, c);
        return c;
    }

    @Retry(name = "service-reseau", fallbackMethod = "routeIndisponible")
    @CircuitBreaker(name = "service-reseau")
    @Bulkhead(name = "service-reseau")
    public RouteDto getRouteByIdStrict(Long id) {
        RouteDto r = reseauRequest.getRouteById(id);
        mettreEnCache("routes", id, r);
        return r;
    }

    private CustomerDto customerDeRepli(Long id, RuntimeException ex) {
        if (!estIndisponible(ex)) throw ex;
        CustomerDto enCache = lireCache("customers", id, CustomerDto.class);
        return enCache != null ? enCache : new CustomerDto(id, null, null, null, null, null);
    }
    private RouteDto routeDeRepli(Long id, RuntimeException ex) {
        if (!estIndisponible(ex)) throw ex;
        RouteDto enCache = lireCache("routes", id, RouteDto.class);
        return enCache != null ? enCache : new RouteDto(id, null, null, null, null, null, null, null);
    }
    private CustomerDto customerIndisponible(Long id, RuntimeException ex) {
        if (!estIndisponible(ex)) throw ex;
        throw new ServiceExterneIndisponibleException("service-clients est momentanément indisponible.");
    }
    private RouteDto routeIndisponible(Long id, RuntimeException ex) {
        if (!estIndisponible(ex)) throw ex;
        throw new ServiceExterneIndisponibleException("service-reseau est momentanément indisponible.");
    }
    private boolean estIndisponible(RuntimeException cause) {
        return cause instanceof CallNotPermittedException
                || (cause instanceof FeignException feign && (feign.status() == -1 || feign.status() == 503));
    }
    private void mettreEnCache(String nom, Object cle, Object valeur) {
        Cache cache = cacheManager.getCache(nom);
        if (cache != null) cache.put(cle, valeur);
    }
    private <X> X lireCache(String nom, Object cle, Class<X> type) {
        Cache cache = cacheManager.getCache(nom);
        return cache != null ? cache.get(cle, type) : null;
    }
}
