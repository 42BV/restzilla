package nl._42.restzilla.web.swagger;

import com.mangofactory.swagger.models.ModelProvider;
import com.mangofactory.swagger.models.dto.ApiListing;
import nl._42.restzilla.web.RestInformation;
import org.springframework.web.bind.annotation.RequestMethod;

import static nl._42.restzilla.web.swagger.SwaggerUtils.newDescription;
import static nl._42.restzilla.web.swagger.SwaggerUtils.addIfNotExists;

/**
 * Describes our dynamically generated controller handle methods.
 *
 * @author Jeroen van Schagen
 * @since Sep 3, 2015
 */
class DefaultApiDescriptor implements SwaggerApiDescriptor {

    @Override
    public void enhance(
      final RestInformation information,
      final ApiListing listing,
      final ModelProvider modelProvider
    ) {
        new Describer(modelProvider, information).enhance(listing);
    }

    private static class Describer {

        private static final String FIND_ALL_NAME = "findAll";
        private static final String FIND_ONE_NAME = "findOne";
        private static final String CREATE_NAME = "create";
        private static final String UPDATE_NAME = "update";
        private static final String DELETE_NAME = "delete";

        private static final String ID_PARAM = "id";
        private static final String PAGE_PARAM = "page";
        private static final String SIZE_PARAM = "size";
        private static final String SORT_PARAM = "sort";

        private final ModelProvider modelProvider;

        private final RestInformation information;

        private final String basePath;

        Describer(ModelProvider modelProvider, RestInformation information) {
            this.modelProvider = modelProvider;
            this.information = information;

            String basePath = information.getBasePath();
            if (!basePath.startsWith("/")) {
                basePath = "/" + basePath;
            }
            this.basePath = basePath;
        }

        /**
         * Enhances the swagger API listings with new models and descriptions.
         *
         * @param listing the API listings to enhance
         */
        void enhance(ApiListing listing) {
            registerFindAll(listing);
            registerFindOne(listing);
            if (!information.isReadOnly()) {
                registerCreate(listing);
                registerUpdate(listing);
                registerDelete(listing);
            }
        }

        private void registerFindAll(ApiListing listing) {
            if (information.findAll().enabled()) {
                addModel(listing, information.getResultType(information.findAll()));
                newDescription(FIND_ALL_NAME, basePath, RequestMethod.GET)
                  .responseClassIterable(information.getResultType(information.findAll()))
                  .addQueryParameter(PAGE_PARAM, Long.class, false)
                  .addQueryParameter(SIZE_PARAM, Long.class, false)
                  .addQueryParameter(SORT_PARAM, String.class, false)
                  .register(listing);
            }
        }

        private void registerFindOne(com.mangofactory.swagger.models.dto.ApiListing listing) {
            if (information.findOne().enabled()) {
                addModel(listing, information.getResultType(information.findOne()));
                newDescription(FIND_ONE_NAME, basePath + "/{id}", RequestMethod.GET)
                  .responseClass(information.getResultType(information.findOne()))
                  .addPathParameter(ID_PARAM, information.getIdentifierClass())
                  .register(listing);
            }
        }

        private void registerCreate(ApiListing listing) {
            if (information.create().enabled()) {
                addModel(listing, information.getInputType(information.create()));
                addModel(listing, information.getResultType(information.create()));
                newDescription(CREATE_NAME, basePath, RequestMethod.POST)
                  .responseClass(information.getResultType(information.create()))
                  .addBodyParameter(information.getInputType(information.create()))
                  .register(listing);
            }
        }

        private void registerUpdate(ApiListing listing) {
            if (information.update().enabled()) {
                addModel(listing, information.getInputType(information.update()));
                addModel(listing, information.getResultType(information.update()));
                newDescription(UPDATE_NAME, basePath + "/{id}", RequestMethod.PUT)
                  .responseClass(information.getResultType(information.update()))
                  .addPathParameter(ID_PARAM, information.getIdentifierClass())
                  .addBodyParameter(information.getInputType(information.update()))
                  .register(listing);
            }
        }

        private void registerDelete(ApiListing listing) {
            if (information.delete().enabled()) {
                addModel(listing, information.getResultType(information.delete()));
                newDescription(DELETE_NAME, basePath + "/{id}", RequestMethod.DELETE)
                  .responseClass(information.getResultType(information.delete()))
                  .addPathParameter(ID_PARAM, information.getIdentifierClass())
                  .register(listing);
            }
        }

        private void addModel(com.mangofactory.swagger.models.dto.ApiListing listing, Class<?> modelType) {
            addIfNotExists(listing, modelType, modelProvider);
        }

    }

}
