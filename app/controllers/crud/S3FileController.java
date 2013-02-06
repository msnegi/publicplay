package controllers.crud;

import java.util.UUID;

import models.S3File;
import models.dao.S3FileDAO;
import play.mvc.Call;
import play.utils.crud.CRUDController;

public class S3FileController extends CRUDController<UUID, S3File> {

	public S3FileController(S3FileDAO dao) {
		super(dao, form(S3File.class), UUID.class, S3File.class);
	}

	@Override
	protected String templateForForm() {
		return "s3FileForm";
	}

	@Override
	protected String templateForList() {
		return "s3FileList";
	}

	@Override
	protected String templateForShow() {
		return "s3FileShow";
	}

	@Override
	protected Call toIndex() {
		return crudIndex();
	}

	public static Call crudIndex() {
		return CategoryController.crudIndex();
	}

}