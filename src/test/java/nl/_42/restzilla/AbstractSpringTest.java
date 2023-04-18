package nl._42.restzilla;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.sql.DataSource;

/**
 * Base for Spring dependent tests.
 *
 * @author Jeroen van Schagen
 * @since Aug 24, 2015
 */
@WebAppConfiguration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ApplicationConfig.class })
public abstract class AbstractSpringTest {
    
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    @AfterEach
    public void cleanUp() {
        jdbcTemplate.execute("TRUNCATE SCHEMA public AND COMMIT");
    }

    @Autowired
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

}

