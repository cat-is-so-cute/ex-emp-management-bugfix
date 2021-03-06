package jp.co.sample.emp_management.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import jp.co.sample.emp_management.domain.Administrator;
import jp.co.sample.emp_management.form.InsertAdministratorForm;
import jp.co.sample.emp_management.form.LoginForm;
import jp.co.sample.emp_management.service.AdministratorService;

/**
 * 管理者情報を操作するコントローラー.
 * 
 * @author igamasayuki
 *
 */
@Controller
@RequestMapping("/")
public class AdministratorController {

	@Autowired
	private AdministratorService administratorService;
	
	@Autowired
	private HttpSession session;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * 使用するフォームオブジェクトをリクエストスコープに格納する.
	 * 
	 * @return フォーム
	 */
	@ModelAttribute
	public InsertAdministratorForm setUpInsertAdministratorForm() {
		return new InsertAdministratorForm();
	}
	
	//  (SpringSecurityに任せるためコメントアウトしました)
	@ModelAttribute
	public LoginForm setUpLoginForm() {
		return new LoginForm();
	}

	/////////////////////////////////////////////////////
	// ユースケース：管理者を登録する
	/////////////////////////////////////////////////////
	/**
	 * 管理者登録画面を出力します.
	 * 
	 * @return 管理者登録画面
	 */
	@RequestMapping("/toInsert")
	public String toInsert() {
		return "administrator/insert";
	}

	/**
	 * 管理者情報を登録します.
	 * 
	 * @param form
	 *            管理者情報用フォーム
	 * @param result
	 *  		     入力値チェックの結果を保持するためのパラメータ
	 * @return ログイン画面へリダイレクト
	 */
	@RequestMapping("/insert")
	public String insert(@Validated InsertAdministratorForm form,
											BindingResult result,
											Model model) {

		if(administratorService.searchByMailAddress(form.getMailAddress()) != null) {
			FieldError disagreementError = new FieldError(result.getObjectName(), "mailAddress", "このメールアドレスは既に使われています");
			result.addError(disagreementError);
		}
		
		if(!form.getPassword().equals(form.getConfirmationPassword())) {
			FieldError duplicationError = new FieldError(result.getObjectName(), "confirmationPassword", "確認用パスワードが一致しません");
			result.addError(duplicationError);			
		}
		
//		if(result.hasErrors()) {
//			return "administrator/insert";
//		}
		
		System.out.println("test1: " + form.toString());

		
		Administrator administrator = new Administrator();
		// フォームからドメインにプロパティ値をコピー
		
		form.setPassword(passwordEncoder.encode(form.getPassword()));
		
		System.out.println("test2: " + form.toString());
		
		BeanUtils.copyProperties(form, administrator);
		administratorService.insert(administrator);
		return "redirect:/";
	}

	/////////////////////////////////////////////////////
	// ユースケース：ログインをする
	/////////////////////////////////////////////////////
	/**
	 * ログイン画面を出力します.
	 * 
	 * @return ログイン画面
	 */
	@RequestMapping("/")
	public String toLogin() {
		return "administrator/login";
	}

	/**
	 * ログインします.
	 * 
	 * @param form
	 *            管理者情報用フォーム
	 * @param result
	 *            エラー情報格納用オブッジェクト
	 * @return ログイン後の従業員一覧画面
	 */
	@RequestMapping("/login")
	public String login(LoginForm form, BindingResult result, Model model) {		
		boolean authenticate = administratorService.authenticate(form.getMailAddress(), form.getPassword());
		if (authenticate == false) {
			model.addAttribute("errorMessage", "メールアドレスまたはパスワードが不正です。");
			return toLogin();
		}
		
		Administrator administrator = administratorService.login(form.getMailAddress());
		session.setAttribute("administratorName", administrator.getName());
		return "forward:/employee/showList";
	}
	
	/////////////////////////////////////////////////////
	// ユースケース：ログアウトをする
	/////////////////////////////////////////////////////
	/**
	 * ログアウトをします. (SpringSecurityに任せるためコメントアウトしました)
	 * 
	 * @return ログイン画面
	 */
	@RequestMapping(value = "/logout")
	public String logout() {
		session.invalidate();
		return "redirect:/";
	}
	
}
