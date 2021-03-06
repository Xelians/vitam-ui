package fr.gouv.vitamui.commons.mongo.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.junit.Test;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;

import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.mongo.domain.Person;
import fr.gouv.vitamui.commons.utils.ReflectionUtils;

public class MongoUtilsTest {

    @Test
    public void testAddCriteria() {
        final List<CriteriaDefinition> criteria = new ArrayList<>();
        MongoUtils.addCriteria(Optional.of("val"), "key", criteria);
        assertThat(criteria.size()).isEqualTo(1);
        assertThat(criteria).containsExactly(Criteria.where("key").is("val"));
    }

    @Test
    public void testAddCriteriaGreaterThan() {
        final List<CriteriaDefinition> criteria = new ArrayList<>();
        MongoUtils.addCriteriaGreaterThan(Optional.of("val"), "key", criteria);
        assertThat(criteria.size()).isEqualTo(1);
        assertThat(criteria).containsExactly(Criteria.where("key").gt("val"));
    }

    @Test
    public void testAddCriteriaIgnoreCase() {
        final List<CriteriaDefinition> criteria = new ArrayList<>();
        MongoUtils.addCriteriaIgnoreCase("key", Optional.of("val"), criteria);
        assertThat(criteria.size()).isEqualTo(1);
        assertThat(criteria)
                .containsExactly(Criteria.where("key").regex(Pattern.compile("^\\Qval\\E$", Pattern.CASE_INSENSITIVE)));
    }

    @Test
    public void testGetCriteriaDefinitionFromEntityClass() {
        fr.gouv.vitamui.commons.api.domain.Criterion c = new fr.gouv.vitamui.commons.api.domain.Criterion(
                "firstName", "alex", CriterionOperator.EQUALS);
        CriteriaDefinition criteria = MongoUtils.getCriteriaDefinitionFromEntityClass(c, Person.class);
        assertThat(criteria.getKey()).isEqualTo("firstName");

        c = new fr.gouv.vitamui.commons.api.domain.Criterion("age", 2, CriterionOperator.EQUALS);
        criteria = MongoUtils.getCriteriaDefinitionFromEntityClass(c, Person.class);
        assertThat(criteria.getKey()).isEqualTo("age");

        c = new fr.gouv.vitamui.commons.api.domain.Criterion("lastConnection", OffsetDateTime.now().toString(),
                CriterionOperator.EQUALS);
        criteria = MongoUtils.getCriteriaDefinitionFromEntityClass(c, Person.class);
        assertThat(criteria.getKey()).isEqualTo("lastConnection");

        c = new fr.gouv.vitamui.commons.api.domain.Criterion("emails", Arrays.asList("julien@vitamui.com"),
                CriterionOperator.EQUALS);
        criteria = MongoUtils.getCriteriaDefinitionFromEntityClass(c, Person.class);
        assertThat(criteria.getKey()).isEqualTo("emails");

        c = new fr.gouv.vitamui.commons.api.domain.Criterion("id", "012absd42", CriterionOperator.EQUALS);
        criteria = MongoUtils.getCriteriaDefinitionFromEntityClass(c, Person.class);
        assertThat(criteria.getKey()).isEqualTo("id");

        c = new fr.gouv.vitamui.commons.api.domain.Criterion("lastName", Arrays.asList("titi", "toto"),
                CriterionOperator.NOTIN);
        criteria = MongoUtils.getCriteriaDefinitionFromEntityClass(c, Person.class);
        assertThat(criteria.getKey()).isEqualTo("lastName");

    }

    @Test
    public void testGetReturnTypeFieldSearch() {
        Type type = MongoUtils.getTypeOfField(Person.class, "address.street");
        assertThat(ReflectionUtils.castTypeToClass(type)).isEqualTo(String.class);

        type = MongoUtils.getTypeOfField(Person.class, "age");
        assertThat(ReflectionUtils.castTypeToClass(type)).isEqualTo(int.class);

        type = MongoUtils.getTypeOfField(Person.class, "addressList.street");
        assertThat(ReflectionUtils.castTypeToClass(type)).isEqualTo(String.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetReturnTypeFielFieldUnknow() {
        MongoUtils.getTypeOfField(Person.class, "unknowfield");
    }

    @Test
    public void getCriteria_when_valueContainsSpecialChar_andUseEqualsOp_then_return_regexpCriteria() {
        String key = "test";
        String val = "toto.*toto";
        Criteria crit = MongoUtils.getCriteria(key, val, CriterionOperator.EQUALS);
        assertThat(crit.getCriteriaObject().toJson()).contains("regex");
        assertThat(crit.getCriteriaObject().toJson()).contains("^" + val + "$");

        val = ".*toto.*to.*to";
        crit = MongoUtils.getCriteria(key, val, CriterionOperator.EQUALS);
        assertThat(crit.getCriteriaObject().toJson()).contains("regex");
        assertThat(crit.getCriteriaObject().toJson()).contains("^" + val + "$");

        val = "toto*toto";
        crit = MongoUtils.getCriteria(key, val, CriterionOperator.EQUALS);
        assertThat(crit.getCriteriaObject().toJson()).contains("regex");
        assertThat(crit.getCriteriaObject().toJson()).contains("^" + "toto.*toto" + "$");

        val = "*toto*toto*";
        crit = MongoUtils.getCriteria(key, val, CriterionOperator.EQUALS);
        assertThat(crit.getCriteriaObject().toJson()).contains("regex");
        assertThat(crit.getCriteriaObject().toJson()).contains("^" + ".*toto.*toto.*" + "$");

    }

    @Test
    public void getCriteria_when_valueContainsSpecialChar_and_useEqualsIgnoreCaseOp_then_return_regexpCriteria() {
        String key = "test";
        String val = "toto.*toto";
        Criteria crit = MongoUtils.getCriteria(key, val, CriterionOperator.EQUALSIGNORECASE);
        assertThat(crit.getCriteriaObject().toJson()).contains("regex");
        assertThat(crit.getCriteriaObject().toJson()).contains("^" + val + "$");

        val = ".*toto.*to.*to";
        crit = MongoUtils.getCriteria(key, val, CriterionOperator.EQUALSIGNORECASE);
        assertThat(crit.getCriteriaObject().toJson()).contains("regex");
        assertThat(crit.getCriteriaObject().toJson()).contains("^" + val + "$");

        val = "toto*toto";
        crit = MongoUtils.getCriteria(key, val, CriterionOperator.EQUALSIGNORECASE);
        assertThat(crit.getCriteriaObject().toJson()).contains("regex");
        assertThat(crit.getCriteriaObject().toJson()).contains("^" + "toto.*toto" + "$");

        val = "*toto*toto*";
        crit = MongoUtils.getCriteria(key, val, CriterionOperator.EQUALSIGNORECASE);
        assertThat(crit.getCriteriaObject().toJson()).contains("regex");
        assertThat(crit.getCriteriaObject().toJson()).contains("^" + ".*toto.*toto.*" + "$");

    }

    @Test
    public void getCriteria_when_valueNotContainsSpecialChar_then_return_eqCriteria() {
        String key = "test";
        String val = "totototo";
        Criteria crit = MongoUtils.getCriteria(key, val, CriterionOperator.EQUALS);
        assertThat(crit.getCriteriaObject().toJson()).contains(val);

    }

}
