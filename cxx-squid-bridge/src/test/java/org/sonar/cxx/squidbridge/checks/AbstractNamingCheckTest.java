/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021 SonarOpenCommunity
 * http://github.com/SonarOpenCommunity/sonar-cxx
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/**
 * fork of SSLR Squid Bridge: https://github.com/SonarSource/sslr-squid-bridge/tree/2.6.1
 * Copyright (C) 2010 SonarSource / mailto: sonarqube@googlegroups.com / license: LGPL v3
 */
package org.sonar.cxx.squidbridge.checks;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.test.minic.MiniCGrammar;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.sonar.cxx.squidbridge.metrics.ResourceParser.scanFile;

public class AbstractNamingCheckTest {

  @org.junit.Rule
  public CheckMessagesVerifierRule checkMessagesVerifier = new CheckMessagesVerifierRule();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static class Check extends AbstractNamingCheck<Grammar> {

    private String regularExpression;

    @Override
    public AstNodeType[] getRules() {
      return new AstNodeType[]{
        MiniCGrammar.BIN_FUNCTION_DEFINITION,
        MiniCGrammar.BIN_VARIABLE_DEFINITION
      };
    }

    @Override
    public String getName(AstNode astNode) {
      return astNode.getTokenValue();
    }

    @Override
    public String getRegexp() {
      return regularExpression;
    }

    @Override
    public String getMessage(String name) {
      return "\"" + name + "\" is a bad name.";
    }

    @Override
    public boolean isExcluded(AstNode astNode) {
      return "LINE".equals(astNode.getTokenValue());
    }

  }

  private final Check check = new Check();

  @Test
  public void detected() {
    check.regularExpression = "[a-z]+";
    checkMessagesVerifier.verify(scanFile("/checks/naming.mc", check).getCheckMessages())
      .next().atLine(5).withMessage("\"BAD\" is a bad name.")
      .next().atLine(12).withMessage("\"myFunction\" is a bad name.");
  }

  @Test
  public void wrong_regular_expression() {
    check.regularExpression = "*";

    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Unable to compile regular expression: *");
    scanFile("/checks/naming.mc", check);
  }

}
