package de.zalando.configuration;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.route53.AmazonRoute53Client;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static de.zalando.util.SpringProfile.DEV;
import static de.zalando.util.SpringProfile.PROD;


@Configuration
public class AwsConfiguration {

    @Bean
    @Profile(DEV)
    public Region region(){
        return Region.getRegion(Regions.EU_CENTRAL_1);
    }

    @Bean
    @Profile(PROD)
    public Region currentRegion(){
        return Regions.getCurrentRegion();
    }

    @Bean
    public AmazonCloudFormation cloudFormation(Region region){
        val client =  new AmazonCloudFormationClient(new DefaultAWSCredentialsProviderChain());
        client.setRegion(region);
        return client;
    }

    @Bean
    public AmazonRoute53Client route53client(Region region){
        val client =  new AmazonRoute53Client(new DefaultAWSCredentialsProviderChain());
        client.setRegion(region);
        return client;
    }

    @Bean
    public AmazonElasticLoadBalancingClient loadBalancingClient(Region region) {
        val client =  new AmazonElasticLoadBalancingClient(new DefaultAWSCredentialsProviderChain());
        client.setRegion(region);
        return client;
    }

}
