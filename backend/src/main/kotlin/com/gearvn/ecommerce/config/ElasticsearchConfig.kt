package com.gearvn.ecommerce.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

@Configuration
@EnableElasticsearchRepositories(basePackages = ["com.gearvn.ecommerce.elasticsearch"])
class ElasticsearchConfig
