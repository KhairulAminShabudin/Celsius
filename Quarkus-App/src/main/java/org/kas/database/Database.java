package org.kas.database;

import org.jboss.resteasy.reactive.MultipartForm;
import org.kas.entity.CelsiusEntity;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;


@Path("database")
public class Database {

    @Inject CelsiusRepository celsiusRepository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        List<CelsiusEntity> celsiusEntity = celsiusRepository.listAll();
        return Response.ok(celsiusEntity).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("id") String id) {
        return celsiusRepository
                .findByIdOptional(Long.valueOf(id))
                .map(celsiusEntity -> Response.ok(celsiusEntity).build())
                .orElse(Response.status(NOT_FOUND).build());
    }

    @GET
    @Path("date/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getByDate(@PathParam("date") String date) {
        List<CelsiusEntity> data = celsiusRepository.findByDate(date);
        return Response.ok(data).build();
    }

    @GET
    @Path("filename/{filename}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getByFileName(@PathParam("filename") String filename) {
        List<CelsiusEntity> data = celsiusRepository.findByDate(filename);
        return Response.ok(data).build();
    }

    @GET
    @Path("metadata")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMetadata() {
        List<HashMap<String, Object>> data = new ArrayList<>();
        List<CelsiusEntity> celsiusEntity = celsiusRepository.listAll();
        for( CelsiusEntity entity : celsiusEntity ){
            HashMap<String, Object> metadata = new HashMap<>();
            metadata.put("filename", entity.getFilename());
            metadata.put("uploadedDate", entity.getUploadedDate());
            data.add(metadata);
        }
        return Response.ok(data).build();
    }

    @POST
    @Transactional
    @Path("/upload-file")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response create(@MultipartForm File csvFile) throws FileNotFoundException {
        CelsiusEntity celsiusEntity = CSVToJson.csv2Json(csvFile);
        celsiusRepository.persist(celsiusEntity);
        if (celsiusRepository.isPersistent(celsiusEntity)) {
            return Response.created(URI.create("/celsiusEntity/" + celsiusEntity.getId())).build();
        }
        return Response.status(NOT_FOUND).build();
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(CelsiusEntity celsiusEntity) {
        celsiusRepository.persist(celsiusEntity);
        if (celsiusRepository.isPersistent(celsiusEntity)) {
            return Response.created(URI.create("/celsiusEntity/" + celsiusEntity.getId())).build();
        }
        return Response.status(NOT_FOUND).build();
    }

    @DELETE
    @Path("filename/{filename}")
    @Transactional
    public Response deleteByFileName(@PathParam("filename") String filename) {
        List<CelsiusEntity> data = celsiusRepository.findByFileName(filename);
        if(data.isEmpty()){
            return Response.status(NOT_FOUND).build();
        }
        celsiusRepository.deleteByFileName(filename);
        return Response.noContent().build();
    }

    @DELETE
    @Path("date/{uploadedDate}")
    @Transactional
    public Response deleteByDate(@PathParam("uploadedDate") String date) {
        List<CelsiusEntity> d = celsiusRepository.findByDate(date);
        if(d.isEmpty()){
            return Response.status(BAD_REQUEST).build();
        }
        celsiusRepository.deleteByDate(date);
        return Response.noContent().build();
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response deleteBydId(@PathParam("id") String id) {
        boolean deleted = celsiusRepository.deleteById(Long.valueOf(id));
        if(deleted) {
            return Response.noContent().build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @DELETE
    @Transactional
    public Response deleteAll() {
        celsiusRepository.deleteAll();
        return Response.noContent().build();
    }
}
