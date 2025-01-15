// package kmusau.translator.configs;

// import org.springframework.boot.context.properties.ConfigurationProperties;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Primary;
// import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

// import javax.sql.DataSource;

// @Configuration
// public class DataSourceConfig {
//     @Bean
//     @ConfigurationProperties(prefix = "spring.datasource.translator")
//     public JndiPropertyHolder primary() {
//         return new JndiPropertyHolder();
//     }

//     @Bean(name = "translatorDataSource")
//     @Primary
//     public DataSource primaryDataSource() {
//         JndiDataSourceLookup dataSourceLookup = new JndiDataSourceLookup();
//         DataSource dataSource = dataSourceLookup.getDataSource(primary().getJndiName());
//         return dataSource;
//     }

//     private static class JndiPropertyHolder {
//         private String jndiName;

//         public String getJndiName() {
//             return jndiName;
//         }

//         public void setJndiName(String jndiName) {
//             this.jndiName = jndiName;
//         }
//     }

// }







