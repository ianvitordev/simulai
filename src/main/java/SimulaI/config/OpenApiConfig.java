package SimulaI.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI simulaIOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SimulaI API")
                        .description("SaaS de geração de simulados de concursos públicos com IA. "
                                + "Questões são geradas exclusivamente pela IA (etapa 9) — não há cadastro manual.")
                        .version("v1")
                        .contact(new Contact().name("SimulaI")));
    }
}
