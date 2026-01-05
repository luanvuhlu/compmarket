package com.luanvv.ecommerce.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

@Configuration
@EnableElasticsearchRepositories(basePackages = ["com.luanvv.ecommerce.elasticsearch"])
class ElasticsearchConfig
